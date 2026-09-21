package com.trackflow.modules.logistics.domain;

/**
 * El centro indicado no está en la ciudad que el recorrido exige para ese movimiento
 * — la de origen, la de destino, o aquella donde el paquete está ahora. Es un 422.
 */
public class CentroFueraDeCiudadException extends RuntimeException {

    private CentroFueraDeCiudadException(String mensaje) {
        super(mensaje);
    }

    /**
     * @param ciudadEsperadaEtiqueta la ciudad concreta que se esperaba, o null cuando
     *        la regla no señala una sola ciudad (el caso del hub intermedio, donde lo
     *        que se prohíbe es una ciudad, no lo que se exige).
     */
    public static CentroFueraDeCiudadException para(EventType tipo, CiudadEsperada esperada, String nombreCentro,
            String ciudadCentro, String ciudadEsperadaEtiqueta) {
        if (esperada == CiudadEsperada.HUB_INTERMEDIO) {
            return new CentroFueraDeCiudadException(
                    ("El centro '%s' ya está en %s, la ciudad de destino del envío. Ese movimiento no es un paso "
                            + "intermedio sino la llegada: registre ARRIVED_AT_DESTINATION_CENTER")
                            .formatted(nombreCentro, ciudadCentro));
        }

        return new CentroFueraDeCiudadException(
                ("El centro '%s' está en %s, pero un movimiento %s debe registrarse en un centro de %s (%s)")
                        .formatted(nombreCentro, ciudadCentro, tipo, ciudadEsperadaEtiqueta, esperada.descripcion()));
    }
}
