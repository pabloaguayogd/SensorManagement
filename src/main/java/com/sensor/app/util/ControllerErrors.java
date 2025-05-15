package com.sensor.app.util;

public class ControllerErrors extends RuntimeException {

    public static final String ERROR_INICIAR_SERVER = "Error al iniciar servidor HTTP: ";
    public static final String ERROR_IN_JSON = "Error en el formato del JSON recibido.";
    public static final String BAD_CONSULT = "Error en la consulta: ";
    public static final String NOT_FOUND = "El objeto de la consulta no se ha encontrado";
    public static final String ERROR_ON_SERVER = "El servidor ha tenido una excepción.";



    public ControllerErrors(String message) {
        super(message);
    }
}
