package tds.biblioteca.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tds.biblioteca.modelo.Autor;
import tds.biblioteca.modelo.Libro;

class BaseDeDatosTest {
	private BaseDeDatos bd;

	@BeforeEach
	void setUp() throws Exception {
		// Base de datos en memoria: se descarta al cerrar la conexión
		bd = new BaseDeDatos("jdbc:sqlite::memory:");
	}

	@AfterEach
	void tearDown() throws Exception {
		bd.close();
	}

	@Test
	void addAutorAsignaId() {
		Autor autor = new Autor("Miguel de Cervantes");
		bd.addAutor(autor);

		assertTrue(autor.getId() > 0);
		List<Autor> autores = bd.getAutores();
		assertEquals(1, autores.size());
		assertEquals("Miguel de Cervantes", autores.get(0).getNombre());
	}

	@Test
	void addLibroGuardaRelacionConAutor() {
		Autor autor = new Autor("Benito Pérez Galdós");
		bd.addAutor(autor);
		Libro libro = new Libro("Fortunata y Jacinta", autor);
		bd.addLibro(libro);

		assertTrue(libro.getId() > 0);
		List<Libro> libros = bd.getLibros();
		assertEquals(1, libros.size());
		assertEquals("Fortunata y Jacinta", libros.get(0).getTitulo());
		assertEquals(autor.getId(), libros.get(0).getAutor().getId());
	}
}
