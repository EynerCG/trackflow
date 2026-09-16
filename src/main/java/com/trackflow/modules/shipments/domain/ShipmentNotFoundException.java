package com.trackflow.modules.shipments.domain;

public class ShipmentNotFoundException extends RuntimeException {

    public ShipmentNotFoundException(String trackingNumber) {
        super("No existe un envío con el número de seguimiento " + trackingNumber);
    }
}
