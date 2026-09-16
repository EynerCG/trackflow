package com.trackflow.modules.reports.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TrackingNotFoundException extends RuntimeException {

    public TrackingNotFoundException(String trackingNumber) {
        super("No se encontró ningún envío con el número de seguimiento " + trackingNumber);
    }
}
