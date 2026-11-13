package tds.tpv.adapters.repository;

import java.util.List;

import tds.tpv.adapters.repository.exceptions.ElementoNoEncontradoException;
import tds.tpv.adapters.repository.exceptions.ElementoExistenteException;
import tds.tpv.adapters.repository.exceptions.ErrorPersistenciaException;
import tds.tpv.negocio.modelo.CarritoCompra;

public interface CompraRepository {

	/**
	 * Devuelve una lista con todas las compras cargadas
	 * 
	 * @return Lista de compras
	 */
	List<? extends CarritoCompra> getCompras();
	/**
	 * Busca una compra por su id y la devuelve
	 * 
	 * @param id
	 * @throws ElementoNoEncontradoException si no encuentra la compra buscada
	 * @return Compra
	 */

	CarritoCompra findById(String id) throws ElementoNoEncontradoException;

	/**
	 * Aniade una compra 
	 * 
	 * @param compra
	 * @throws ElementoExistenteException si existe una compra igual en el
	 *                                    repositorio
	 * @return void
	 */
	void addCompra(CarritoCompra compra) throws ElementoExistenteException,ErrorPersistenciaException;
	
	/**
	 * Elimina una compra de la lista de compras, si ya estaba eliminada no
	 * hace nada
	 * 
	 * @param compra
	 * @return void
	 */

	void removeCompra(CarritoCompra compra) throws ErrorPersistenciaException;
	
	/**
	 * Actualiza una compra de la lista de compras, devolviendo la referencia a la compra que
	 * se ha actualizado
	 * 
	 * @param compra
	 * @return Compra
	 */
	CarritoCompra updateCompra(CarritoCompra compra) throws ErrorPersistenciaException;
}



