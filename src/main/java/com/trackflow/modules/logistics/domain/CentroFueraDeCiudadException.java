package com.trackflow.modules.logistics.domain;

/**
 * El centro indicado no está en la ciudad que la regla de negocio exige: un
 * ARRIVED_AT_DESTINATION_CENTER debe ocurrir en la ciudad de destino del envío, y el
 * primer RECEIVED_AT_CENTER en la ciudad de origen. Es un 422.
 */
public class CentroFueraDeCiudadException extends RuntimeException {

    private CentroFueraDeCiudadException(String mensaje) {
        super(mensaje);
    }

    public static CentroFueraDeCiudadException paraCentroDeDestino(String nombreCentro, String ciudadCentro,
            String ciudadDestinoEnvio) {
        return new CentroFueraDeCiudadException(
                ("El centro '%s' está en %s, pero el destino del envío es %s. Un evento "
                        + "ARRIVED_AT_DESTINATION_CENTER debe registrarse en un centro de la ciudad de destino")
                        .formatted(nombreCentro, ciudadCentro, ciudadDestinoEnvio));
    }

    public static CentroFueraDeCiudadException paraCentroDeOrigen(String nombreCentro, String ciudadCentro,
            String ciudadOrigenEnvio) {
        return new CentroFueraDeCiudadException(
                ("El centro '%s' está en %s, pero el origen del envío es %s. El primer evento "
                        + "RECEIVED_AT_CENTER debe registrarse en un centro de la ciudad de origen")
                        .formatted(nombreCentro, ciudadCentro, ciudadOrigenEnvio));
    }
}
