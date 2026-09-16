package com.trackflow.modules.reports.domain;

public class TrackingNotFoundException extends RuntimeException {

    public TrackingNotFoundException(String trackingNumber) {
        super("No se encontró ningún envío con el número de seguimiento " + trackingNumber);
    }
}
