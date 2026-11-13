package tds.tpv.adapters.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tds.tpv.adapters.repository.ProductoRepository;
import tds.tpv.adapters.repository.exceptions.ElementoExistenteException;
import tds.tpv.adapters.repository.exceptions.ElementoNoEncontradoException;
import tds.tpv.adapters.repository.exceptions.ErrorPersistenciaException;
import tds.tpv.negocio.modelo.Producto;
import tds.tpv.negocio.modelo.impl.ProductoImpl;

/**
 * Repositorio de productos que persiste en SQLite.
 *
 * Los productos se mantienen también en memoria porque el resto de la aplicación
 * los compara por identidad (==), por lo que cada código debe corresponder siempre
 * a la misma instancia. Las modificaciones se escriben primero en la base de datos
 * y solo si tienen éxito se aplican a la lista en memoria.
 */
public class ProductoRepositorySQLiteImpl implements ProductoRepository {
	private static final Logger log = LogManager.getLogger();

	private final BaseDatosSQLite baseDatos;
	private List<ProductoImpl> productos;

	public ProductoRepositorySQLiteImpl(BaseDatosSQLite baseDatos) {
		this.baseDatos = baseDatos;
		try {
			this.productos = cargarProductos();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<? extends Producto> getProductos() {
		return productos;
	}

	@Override
	public Producto findById(String codigo) throws ElementoNoEncontradoException {
		return buscar(codigo)
				.orElseThrow(() -> new ElementoNoEncontradoException("El producto " + codigo + " no existe"));
	}

	@Override
	public void addProducto(Producto producto) throws ElementoExistenteException, ErrorPersistenciaException {
		// Si el producto ya existe no puedo insertarlo
		if (buscar(producto.getCodigo()).isPresent()) {
			throw new ElementoExistenteException("El producto " + producto.getCodigo() + " ya existe en el inventario");
		}

		try (Connection conexion = baseDatos.abrirConexion();
				PreparedStatement insercion = conexion.prepareStatement("INSERT INTO producto (codigo, descripcion, cantidad, precio) VALUES (?, ?, ?, ?)")) {
			asignarValores(insercion, producto);
			insercion.executeUpdate();
		} catch (SQLException e) {
			log.error("Error persistiendo el producto {}", producto.getCodigo(), e);
			// Capturo las excepciones genericas lanzadas al persistir y lanzo una propia
			// encapsulando la excepcion real
			throw new ErrorPersistenciaException(e);
		}
		productos.add((ProductoImpl) producto);
	}

	@Override
	public void removeProducto(Producto producto) throws ErrorPersistenciaException {
		// Falla si el producto aparece en alguna compra, por la clave ajena de la tabla item
		try (Connection conexion = baseDatos.abrirConexion();
				PreparedStatement borrado = conexion.prepareStatement("DELETE FROM producto WHERE codigo = ?")) {
			borrado.setString(1, producto.getCodigo());
			borrado.executeUpdate();
		} catch (SQLException e) {
			log.error("Error eliminando el producto {}", producto.getCodigo(), e);
			throw new ErrorPersistenciaException(e);
		}
		productos.removeIf(p -> p.getCodigo().equals(producto.getCodigo()));
	}

	@Override
	public Producto updateProducto(Producto producto) throws ErrorPersistenciaException {
		ProductoImpl existente = buscar(producto.getCodigo())
				.orElseThrow(() -> new ErrorPersistenciaException("El producto " + producto.getCodigo() + " no existe"));

		// No borramos los productos con stock 0
		String actualizacion = "UPDATE producto SET descripcion = ?, cantidad = ?, precio = ? WHERE codigo = ?";
		try (Connection conexion = baseDatos.abrirConexion();
				PreparedStatement sentencia = conexion.prepareStatement(actualizacion)) {
			sentencia.setString(1, producto.getDescripcion());
			sentencia.setInt(2, producto.getCantidad());
			sentencia.setDouble(3, producto.getPrecio());
			sentencia.setString(4, producto.getCodigo());
			sentencia.executeUpdate();
		} catch (SQLException e) {
			log.error("Error actualizando el producto {}", producto.getCodigo(), e);
			throw new ErrorPersistenciaException(e);
		}

		// Se conserva la instancia en memoria aunque se reciba otra con el mismo codigo
		existente.setDescripcion(producto.getDescripcion());
		existente.setCantidad(producto.getCantidad());
		existente.setPrecio(producto.getPrecio());
		return existente;
	}

	private Optional<ProductoImpl> buscar(String codigo) {
		return productos.stream()
				.filter(prod -> prod.getCodigo().equals(codigo))
				.findFirst();
	}

	private List<ProductoImpl> cargarProductos() throws SQLException {
		try (Connection conexion = baseDatos.abrirConexion()) {
			List<ProductoImpl> resultado = new ArrayList<>();
			String consulta = "SELECT codigo, descripcion, cantidad, precio FROM producto ORDER BY codigo";
			try (Statement sentencia = conexion.createStatement();
					ResultSet filas = sentencia.executeQuery(consulta)) {
				while (filas.next()) {
					String codigo = filas.getString("codigo");
					String descripcion = filas.getString("descripcion");
					int cantidad = filas.getInt("cantidad");
					double precio = filas.getDouble("precio");

					resultado.add(new ProductoImpl(codigo, descripcion, cantidad, precio));
				}
			}
			return resultado;
		}
	}

	private void asignarValores(PreparedStatement insercion, Producto producto) throws SQLException {
		insercion.setString(1, producto.getCodigo());
		insercion.setString(2, producto.getDescripcion());
		insercion.setInt(3, producto.getCantidad());
		insercion.setDouble(4, producto.getPrecio());
	}

}
