package tds.tpv.adapters.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tds.tpv.adapters.repository.CompraRepository;
import tds.tpv.adapters.repository.ProductoRepository;
import tds.tpv.adapters.repository.exceptions.ElementoExistenteException;
import tds.tpv.adapters.repository.exceptions.ElementoNoEncontradoException;
import tds.tpv.adapters.repository.exceptions.ErrorPersistenciaException;
import tds.tpv.negocio.modelo.CarritoCompra;
import tds.tpv.negocio.modelo.impl.CarritoCompraImpl;
import tds.tpv.negocio.modelo.impl.ItemImpl;

/**
 * Repositorio de compras que persiste en SQLite.
 *
 * Los productos de los items se obtienen de {@link ProductoRepository} para que
 * sean las mismas instancias que usa el resto de la aplicación. Una compra y sus
 * items se escriben siempre en una única transacción.
 */
public class CompraRepositorySQLiteImpl implements CompraRepository {
	private static final Logger log = LogManager.getLogger();

	private final BaseDatosSQLite baseDatos;
	private final ProductoRepository repositorioProductos;
	private final List<CarritoCompra> compras;

	public CompraRepositorySQLiteImpl(BaseDatosSQLite baseDatos, ProductoRepository repositorioProductos) {
		this.baseDatos = baseDatos;
		this.repositorioProductos = repositorioProductos;
		try {
			this.compras = cargarCompras();
		} catch (SQLException | ElementoNoEncontradoException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<? extends CarritoCompra> getCompras() {
		return compras;
	}
	
	/**
	 * @param id Parte numérica del identificador de la compra: 7 busca "C00007"
	 */
	@Override
	public CarritoCompra findById(String id) throws ElementoNoEncontradoException {
		return getCompras().stream()
				.filter(c -> c.getIdCompra().equals(id))
				.findFirst()
				.orElseThrow(() -> new ElementoNoEncontradoException("La compra " + id + " no existe"));
	}

	@Override
	public void addCompra(CarritoCompra compra) throws ElementoExistenteException, ErrorPersistenciaException {
		// Si la compra ya existe no puedo insertarla
		if (buscar(compra.getIdCompra()).isPresent()) {
			throw new ElementoExistenteException("La compra " + compra.getIdCompra() + " ya existe");
		}

		try (Connection conexion = baseDatos.abrirConexion();
				PreparedStatement insercion = conexion.prepareStatement("INSERT INTO compra (id_compra) VALUES (?)")) {
			conexion.setAutoCommit(false);
			insercion.setString(1, compra.getIdCompra());
			insercion.executeUpdate();
			insertarItems(conexion, compra);
			conexion.commit();
		} catch (SQLException e) {
			log.error("Error persistiendo la compra {}", compra.getIdCompra(), e);
			// Capturo las excepciones genericas lanzadas al persistir y lanzo una propia
			// encapsulando la excepcion real
			throw new ErrorPersistenciaException(e);
		}
		compras.add(compra);
	}

	@Override
	public void removeCompra(CarritoCompra compra) throws ErrorPersistenciaException {
		// Los items se borran en cascada
		try (Connection conexion = baseDatos.abrirConexion();
				PreparedStatement borrado = conexion.prepareStatement("DELETE FROM compra WHERE id_compra = ?")) {
			borrado.setString(1, compra.getIdCompra());
			borrado.executeUpdate();
		} catch (SQLException e) {
			log.error("Error eliminando la compra {}", compra.getIdCompra(), e);
			throw new ErrorPersistenciaException(e);
		}
		compras.removeIf(c -> c.getIdCompra().equals(compra.getIdCompra()));
	}

	@Override
	public CarritoCompra updateCompra(CarritoCompra compra) throws ErrorPersistenciaException {
		CarritoCompra existente = buscar(compra.getIdCompra())
				.orElseThrow(() -> new ErrorPersistenciaException("La compra " + compra.getIdCompra() + " no existe"));

		// Lo unico que puede cambiar de una compra son sus items: se sustituyen todos
		try (Connection conexion = baseDatos.abrirConexion();
				PreparedStatement borrado = conexion.prepareStatement("DELETE FROM item WHERE id_compra = ?")) {
			conexion.setAutoCommit(false);
			borrado.setString(1, compra.getIdCompra());
			borrado.executeUpdate();
			insertarItems(conexion, compra);
			conexion.commit();
		} catch (SQLException e) {
			log.error("Error actualizando la compra {}", compra.getIdCompra(), e);
			throw new ErrorPersistenciaException(e);
		}
		compras.set(compras.indexOf(existente), compra);
		return compra;
	}

	private Optional<CarritoCompra> buscar(String idCompra) {
		return compras.stream()
				.filter(c -> c.getIdCompra().equals(idCompra))
				.findFirst();
	}

	private void insertarItems(Connection conexion, CarritoCompra compra) throws SQLException {
		String insercionItem = "INSERT INTO item (id_item, id_compra, codigo_producto, cantidad) VALUES (?, ?, ?, ?)";
		try (PreparedStatement insercion = conexion.prepareStatement(insercionItem)) {
			for (ItemImpl item : ((CarritoCompraImpl) compra).getItems()) {
				insercion.setString(1, item.getIdItem());
				insercion.setString(2, compra.getIdCompra());
				insercion.setString(3, item.getProducto().getCodigo());
				insercion.setInt(4, item.getCantidad());
				insercion.executeUpdate();
			}
		}
	}

	private List<CarritoCompra> cargarCompras() throws SQLException, ElementoNoEncontradoException {
		try (Connection conexion = baseDatos.abrirConexion(); Statement sentencia = conexion.createStatement()) {
			Map<String, CarritoCompraImpl> resultado = new 	HashMap<>();
			try (ResultSet filas = sentencia.executeQuery("SELECT id_compra FROM compra ORDER BY id_compra")) {
				while (filas.next()) {
					String idCompra = filas.getString("id_compra");

					CarritoCompraImpl compra = new CarritoCompraImpl();
					compra.setIdCompra(idCompra);
					resultado.put(idCompra, compra);
				}
			}

			String consultaItems = "SELECT id_item, id_compra, codigo_producto, cantidad FROM item ORDER BY id_item";
			try (ResultSet filas = sentencia.executeQuery(consultaItems)) {
				while (filas.next()) {
					String idItem = filas.getString("id_item");
					String idCompra = filas.getString("id_compra");
					String codigoProducto = filas.getString("codigo_producto");
					int cantidad = filas.getInt("cantidad");

					ItemImpl item = new ItemImpl(repositorioProductos.findById(codigoProducto), cantidad);
					item.setIdItem(idItem);
					resultado.get(idCompra).addItem(item);
				}
			}
			return new ArrayList<>(resultado.values());
		}
	}

}
