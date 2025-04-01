package com.sensor.sensor_app;

import java.util.Objects;

public class Dispositivo {

	private int id;
	private String nombre;
	private int id_grupo;
	
	public Dispositivo() {
		// TODO Auto-generated constructor stub
	}
	
	// TODO Añadir más constructores

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getId_grupo() {
		return id_grupo;
	}

	public void setId_grupo(int id_grupo) {
		this.id_grupo = id_grupo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, id_grupo, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dispositivo other = (Dispositivo) obj;
		return id == other.id && id_grupo == other.id_grupo && Objects.equals(nombre, other.nombre);
	}

	@Override
	public String toString() {
		return "Dispositivo [id=" + id + ", nombre=" + nombre + ", id_grupo=" + id_grupo + "]";
	}


	
}
