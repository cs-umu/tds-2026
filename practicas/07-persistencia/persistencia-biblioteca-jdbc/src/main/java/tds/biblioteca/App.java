package tds.biblioteca;

import tds.biblioteca.modelo.Autor;
import tds.biblioteca.modelo.Libro;
import tds.biblioteca.persistencia.BaseDeDatos;

public class App {

	public static void main(String[] args) throws Exception {
		try (BaseDeDatos bd = new BaseDeDatos()) {
			Autor cervantes = new Autor("Miguel de Cervantes");
			Autor galdos = new Autor("Benito Pérez Galdós");
			bd.addAutor(cervantes);
			bd.addAutor(galdos);

			bd.addLibro(new Libro("Don Quijote de la Mancha", cervantes));
			bd.addLibro(new Libro("Novelas ejemplares", cervantes));
			bd.addLibro(new Libro("Fortunata y Jacinta", galdos));

			System.out.println("Autores:");
			bd.getAutores().forEach(a -> System.out.println("  " + a));

			System.out.println("Libros:");
			bd.getLibros().forEach(l -> System.out.println("  " + l));
		}
	}
}
