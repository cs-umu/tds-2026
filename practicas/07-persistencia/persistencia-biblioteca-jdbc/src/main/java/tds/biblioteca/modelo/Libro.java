package tds.biblioteca.modelo;

public class Libro {
	private long id;
	private String titulo;
	private Autor autor;

	/** Libro todavía no persistido: el id lo asigna la base de datos. */
	public Libro(String titulo, Autor autor) {
		this.titulo = titulo;
		this.autor = autor;
	}

	public Libro(long id, String titulo, Autor autor) {
		this.id = id;
		this.titulo = titulo;
		this.autor = autor;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public Autor getAutor() {
		return autor;
	}

	@Override
	public String toString() {
		return "Libro [id=" + id + ", titulo=" + titulo + ", autor=" + autor.getNombre() + "]";
	}
}
