package com.trackflow.modules.logistics.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UnknownShipmentException extends RuntimeException {

    public UnknownShipmentException(String trackingNumber) {
        super("No existe un envío con el número de seguimiento " + trackingNumber);
    }
}
