package com.trackflow.modules.shipments.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShipmentNotFoundException extends RuntimeException {

    public ShipmentNotFoundException(String trackingNumber) {
        super("No existe un envío con el número de seguimiento " + trackingNumber);
    }
}
