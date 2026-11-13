package tds.tpv.adapters.repository.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Representa el fichero SQLite compartido por los repositorios y define sus tablas.
 */
public class BaseDatosSQLite {
	private static final Logger log = LogManager.getLogger();

	private static final String CREAR_TABLA_PRODUCTO = """
			CREATE TABLE IF NOT EXISTS producto (
				codigo      TEXT PRIMARY KEY,
				descripcion TEXT NOT NULL,
				cantidad    INTEGER NOT NULL CHECK (cantidad >= 0),
				precio      REAL NOT NULL
			)""";
	private static final String CREAR_TABLA_COMPRA = """
			CREATE TABLE IF NOT EXISTS compra (
				id_compra TEXT PRIMARY KEY
			)""";
	private static final String CREAR_TABLA_ITEM = """
			CREATE TABLE IF NOT EXISTS item (
				id_item         TEXT PRIMARY KEY,
				id_compra       TEXT NOT NULL REFERENCES compra (id_compra) ON DELETE CASCADE,
				codigo_producto TEXT NOT NULL REFERENCES producto (codigo),
				cantidad        INTEGER NOT NULL
			)""";

	private final String url;

	/**
	 * @param fichero Ruta del fichero de la base de datos. Si no existe se crea.
	 */
	public BaseDatosSQLite(String fichero) {
		this.url = "jdbc:sqlite:" + fichero;
		crearTablas();
	}

	private void crearTablas() {
		try (Connection conexion = abrirConexion(); Statement sentencia = conexion.createStatement()) {
			sentencia.execute(CREAR_TABLA_PRODUCTO);
			sentencia.execute(CREAR_TABLA_COMPRA);
			sentencia.execute(CREAR_TABLA_ITEM);
		} catch (SQLException e) {
			// No se propaga porque los repositorios informaran del error al usar la base de datos
			log.error("No se han podido crear las tablas ", e);
		}
	}

	/**
	 * Abre una nueva conexión, que debe cerrar quien la solicita. Si se cierra con
	 * una transacción sin confirmar, sus cambios se descartan.
	 */
	Connection abrirConexion() throws SQLException {
		// SQLite no comprueba las claves ajenas salvo que se active en cada conexión
		Properties propiedades = new Properties();
		propiedades.setProperty("foreign_keys", "true");
		return DriverManager.getConnection(url, propiedades);
	}

}
