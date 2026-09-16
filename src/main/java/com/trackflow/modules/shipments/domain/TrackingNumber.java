package com.trackflow.modules.shipments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public class TrackingNumber {

    public static final String PREFIX = "TF";
    private static final Pattern FORMAT = Pattern.compile("^TF[0-9A-Z]{12}$");

    @Column(name = "tracking_number", nullable = false, unique = true, updatable = false)
    private String value;

    protected TrackingNumber() {
    }

    private TrackingNumber(String value) {
        this.value = value;
    }

    public static TrackingNumber of(String value) {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Número de seguimiento inválido: " + value);
        }
        return new TrackingNumber(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TrackingNumber that && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
