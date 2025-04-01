package com.sensor.sensor_app;

import java.util.Objects;

public class Grupo {
	private int id;
	private String canal_mqtt;
	private String nombre;
	
	public Grupo() {
		// TODO Auto-generated constructor stub
	}
	
	// TODO Añadir más constructores

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCanal_mqtt() {
		return canal_mqtt;
	}

	public void setCanal_mqtt(String canal_mqtt) {
		this.canal_mqtt = canal_mqtt;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@Override
	public int hashCode() {
		return Objects.hash(canal_mqtt, id, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Grupo other = (Grupo) obj;
		return Objects.equals(canal_mqtt, other.canal_mqtt) && id == other.id && Objects.equals(nombre, other.nombre);
	}

	@Override
	public String toString() {
		return "Grupo [id=" + id + ", canal_mqtt=" + canal_mqtt + ", nombre=" + nombre + "]";
	}

	
}
