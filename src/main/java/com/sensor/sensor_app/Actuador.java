package com.sensor.sensor_app;

import java.util.Objects;

public class Actuador {
	private int id;
	private String nombre;
	private String tipo;
	private String identificador;
	private int id_dispositivo;
	
	
	public Actuador() {
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


	public String getTipo() {
		return tipo;
	}


	public void setTipo(String tipo) {
		this.tipo = tipo;
	}


	public String getIdentificador() {
		return identificador;
	}


	public void setIdentificador(String identificador) {
		this.identificador = identificador;
	}


	public int getId_dispositivo() {
		return id_dispositivo;
	}


	public void setId_dispositivo(int id_dispositivo) {
		this.id_dispositivo = id_dispositivo;
	}


	@Override
	public int hashCode() {
		return Objects.hash(id, id_dispositivo, identificador, nombre, tipo);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Actuador other = (Actuador) obj;
		return id == other.id && id_dispositivo == other.id_dispositivo
				&& Objects.equals(identificador, other.identificador) && Objects.equals(nombre, other.nombre)
				&& Objects.equals(tipo, other.tipo);
	}


	public String toString() {
		return "Actuador [id=" + id + ", nombre=" + nombre + ", tipo=" + tipo + ", identificador=" + identificador
				+ ", id_dispositivo=" + id_dispositivo + "]";
	}

	
	
}
