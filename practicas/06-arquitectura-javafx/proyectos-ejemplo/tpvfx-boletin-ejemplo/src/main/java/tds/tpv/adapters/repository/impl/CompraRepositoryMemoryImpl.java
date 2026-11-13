package tds.tpv.adapters.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tds.tpv.adapters.repository.CompraRepository;
import tds.tpv.adapters.repository.exceptions.ElementoExistenteException;
import tds.tpv.adapters.repository.exceptions.ElementoNoEncontradoException;
import tds.tpv.adapters.repository.exceptions.ErrorPersistenciaException;
import tds.tpv.negocio.modelo.CarritoCompra;

public class CompraRepositoryMemoryImpl implements CompraRepository {
	private static final Logger log = LogManager.getLogger();

	private List<CarritoCompra> compras = null;
	
	private void cargaCompras() throws ErrorPersistenciaException {
		try {
			this.compras = cargarCompras();
		} catch (Exception e) {
			log.error("Error cargando las compras ", e);
			throw new ErrorPersistenciaException(e);
		}
	}

	@Override
	public List<? extends CarritoCompra> getCompras() {
		if (compras == null) {
			try {
				cargaCompras();
			} catch (ErrorPersistenciaException e) {
				// Manejo la excepcion pero no la propago porque en este sitio es donde se puede
				// gestionar mejor
				log.error("No se han podido cargar las compras ", e);
			}
		}
		return compras;
	}

	@Override
	public CarritoCompra findById(String id) throws ElementoNoEncontradoException { 
		Optional<? extends CarritoCompra> compra = getCompras().stream()
				.filter(c -> c.getIdCompra().equals(id))
				.findFirst();
	
		if (compra.isEmpty())
			throw new ElementoNoEncontradoException("La compra " + id + " no existe");
		return compra.orElse(null);

	}

	@Override
	public void addCompra(CarritoCompra compra) throws ElementoExistenteException,ErrorPersistenciaException {
		// Si el producto ya existe no puedo insertarlo
		if (compras.contains(compra)) {
			throw new ElementoExistenteException("La compra " + compra.getIdCompra() + " ya existe");
		}
		
		compras.add(compra);
		try {
			guardarCompras(compras);
		} catch (Exception e) {
			log.error("Error persistiendo la compra{}", compra.getIdCompra(), e);
			// Capturo las excepciones genericas lanzadas al persistir y lanzo una propia
			// encapsulando la excepcion real
			throw new ErrorPersistenciaException(e);
		}
	}

	@Override
	public void removeCompra(CarritoCompra compra) throws ErrorPersistenciaException {
		if (!compras.contains(compra)) 
			return ;
		
		compras.remove(compra);
		try {
			guardarCompras(compras);
		} catch (Exception e) {
			log.error("Error eliminando la compra {}", compra.getIdCompra(), e);
			throw new ErrorPersistenciaException(e);
		}
	}

	@Override
	public CarritoCompra updateCompra(CarritoCompra compra) throws ErrorPersistenciaException {
		try {
			guardarCompras(compras);
			return compra;
		} catch (Exception e) {
			log.error("Error eliminando la compra {}", compra.getIdCompra(), e);
			throw new ErrorPersistenciaException(e);
		}
	}

	// Metodo para cargar las compras incialmente : en memoria no tiene efecto	
	private List<CarritoCompra> cargarCompras() throws Exception {
		if (compras == null) compras = new ArrayList<CarritoCompra>();
		return compras;
	}
	
	// Metodo para guardar las compras : en memoria no tiene efecto
	private void guardarCompras(List<CarritoCompra> compras) throws Exception {
		this.compras = compras;
	}

}
