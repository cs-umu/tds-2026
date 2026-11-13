package tds.tpv;

import tds.tpv.adapters.repository.ProductoRepository;
import tds.tpv.adapters.repository.impl.BaseDatosSQLite;
import tds.tpv.adapters.repository.impl.CompraRepositorySQLiteImpl;
import tds.tpv.adapters.repository.impl.ProductoRepositorySQLiteImpl;
import tds.tpv.negocio.controladores.ControladorTPV;

public class ConfiguracionSQLiteImpl extends Configuracion {

	private static final String FICHERO_BASE_DATOS = "tpv.db";

	private ControladorTPV controlador;

	public ConfiguracionSQLiteImpl() {
		BaseDatosSQLite baseDatos = new BaseDatosSQLite(FICHERO_BASE_DATOS);
		ProductoRepository productos = new ProductoRepositorySQLiteImpl(baseDatos);
		this.controlador = new ControladorTPV(productos, new CompraRepositorySQLiteImpl(baseDatos, productos));
	}

	@Override
	public ControladorTPV getControladorTPV() {
		return controlador;
	}

}
