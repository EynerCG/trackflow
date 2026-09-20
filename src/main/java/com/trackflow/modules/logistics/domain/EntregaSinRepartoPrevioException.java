package com.trackflow.modules.logistics.domain;

/**
 * No se puede marcar un envío como entregado sin que antes haya salido a reparto. Es
 * un 422: el envío existe y el tipo de evento es válido, pero no en este orden.
 */
public class EntregaSinRepartoPrevioException extends RuntimeException {

    public EntregaSinRepartoPrevioException(String trackingNumber) {
        super(("No se puede registrar DELIVERED para el envío %s porque no tiene un movimiento "
                + "OUT_FOR_DELIVERY previo registrado").formatted(trackingNumber));
    }
}
