package tds.tpv.negocio.modelo.impl;

import java.util.ArrayList;
import java.util.List;

import tds.tpv.negocio.modelo.CarritoCompra;
import tds.tpv.negocio.modelo.Producto;

public class CarritoCompraImpl implements CarritoCompra {
	
	private String idCompra;
	
	private List<ItemImpl> items;

	public CarritoCompraImpl() {
		this.idCompra = "";
		this.items = new ArrayList<>();
	}
	
	public String getIdCompra() {
		return idCompra;
	}

	public void setIdCompra(String id) {
		this.idCompra = id;
	}

	public void addItem(ItemImpl item) {
		this.items.add(item);
	}
	
	public List<? extends ItemImpl> getItems() {
		return items;
	}

	/**
	 * Añade un item carrito comprobando que si ya hay un item para ese producto
	 * se incrementa su cantidad en lugar de añadir otro.
	 */
	public ItemImpl addItem(Producto producto, int cantidad) {
		for (ItemImpl item : items) {
			if (item.getProducto() == producto) {
				item.incrementarCantidad(cantidad);
				return item;
			}
		}
		ItemImpl item = new ItemImpl(producto, cantidad);
		this.items.add(item);
		return item;
	}

	public void removeItem(Producto producto) {
		ItemImpl itemSeleccionado=null;
		for (ItemImpl item : items) {
			if (item.getProducto() == producto) {
				itemSeleccionado = item;
				break;
			}
		}
		if (itemSeleccionado != null) {
			this.items.remove(itemSeleccionado);
		}
	}
	
	@Override
	public double getTotal() {
		return getItems().stream().mapToDouble(item -> item.getPrecioTotal()).sum();
	}
}
