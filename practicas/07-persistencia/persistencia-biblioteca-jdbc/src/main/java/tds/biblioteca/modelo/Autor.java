package tds.biblioteca.modelo;

public class Autor {
	private long id;
	private String nombre;

	/** Autor todavía no persistido: el id lo asigna la base de datos. */
	public Autor(String nombre) {
		this.nombre = nombre;
	}

	public Autor(long id, String nombre) {
		this.id = id;
		this.nombre = nombre;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	@Override
	public String toString() {
		return "Autor [id=" + id + ", nombre=" + nombre + "]";
	}
}
