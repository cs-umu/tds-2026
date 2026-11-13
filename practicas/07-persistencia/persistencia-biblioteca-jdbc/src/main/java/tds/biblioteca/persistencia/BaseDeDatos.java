package tds.biblioteca.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tds.biblioteca.modelo.Autor;
import tds.biblioteca.modelo.Libro;

public class BaseDeDatos implements AutoCloseable {
	private static final String URL = "jdbc:sqlite:basedatos.db";
	private Connection conexion;

	public BaseDeDatos() throws SQLException {
		this(URL);
	}

	/** Permite indicar otra JDBC URL (p.ej., en los tests). */
	public BaseDeDatos(String url) throws SQLException {
		conexion = DriverManager.getConnection(url);
		crearTablas();
	}

	private void crearTablas() throws SQLException {
		String sqlAutores = """
				CREATE TABLE IF NOT EXISTS Autores (
				  id INTEGER PRIMARY KEY AUTOINCREMENT,
				  nombre TEXT NOT NULL
				);""";

		String sqlLibros = """
				CREATE TABLE IF NOT EXISTS Libros (
				  id INTEGER PRIMARY KEY AUTOINCREMENT,
				  titulo TEXT NOT NULL,
				  autor_id INTEGER,
				  FOREIGN KEY (autor_id) REFERENCES Autores(id)
				);""";

		try (Statement stmt = conexion.createStatement()) {
			stmt.execute(sqlAutores);
			stmt.execute(sqlLibros);
		}
	}

	public void addAutor(Autor autor) {
		String sql = "INSERT INTO Autores (nombre) VALUES (?)";
		try (PreparedStatement pstmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, autor.getNombre());
			pstmt.executeUpdate();
			try (ResultSet rs = pstmt.getGeneratedKeys()) {
				if (rs.next()) {
					autor.setId(rs.getLong(1));
				} else {
					throw new IllegalStateException();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void addLibro(Libro libro) {
		String sql = "INSERT INTO Libros (titulo, autor_id) VALUES (?, ?)";
		try (PreparedStatement pstmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, libro.getTitulo());
			pstmt.setLong(2, libro.getAutor().getId());
			pstmt.executeUpdate();
			try (ResultSet rs = pstmt.getGeneratedKeys()) {
				if (rs.next()) {
					long codigo = rs.getLong(1);
					libro.setId(codigo);
				} else {
					throw new IllegalStateException();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public List<Autor> getAutores() {
		String consulta = "SELECT id, nombre FROM Autores";

		try (Statement stmt = conexion.createStatement();
				ResultSet rs = stmt.executeQuery(consulta)) {

			List<Autor> autores = new ArrayList<>();
			while (rs.next()) {
				Autor autor = new Autor(
						rs.getLong("id"),
						rs.getString("nombre"));
				autores.add(autor);
			}

			return autores;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public List<Libro> getLibros() {
		String consulta = """
				SELECT l.id, l.titulo, a.id AS autor_id, a.nombre
				FROM Libros l JOIN Autores a ON l.autor_id = a.id""";

		try (Statement stmt = conexion.createStatement();
				ResultSet rs = stmt.executeQuery(consulta)) {

			List<Libro> libros = new ArrayList<>();
			while (rs.next()) {
				// Mapeo manual: con cada fila se reconstruyen el libro y su autor
				Autor autor = new Autor(rs.getLong("autor_id"), rs.getString("nombre"));
				libros.add(new Libro(rs.getLong("id"), rs.getString("titulo"), autor));
			}
			return libros;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws Exception {
		try {
			if (conexion != null) {
				conexion.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
