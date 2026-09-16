package com.trackflow.modules.logistics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Envíos que logistics conoce, alimentado por los eventos que publica shipments.
 * Evita que este módulo dependa del modelo de envíos para validar que existen.
 */
@Entity
@Table(name = "logistics_tracked_shipments")
public class TrackedShipment {

    @Id
    private String trackingNumber;

    @Column(nullable = false)
    private Instant registeredAt;

    protected TrackedShipment() {
    }

    public TrackedShipment(String trackingNumber, Instant registeredAt) {
        this.trackingNumber = trackingNumber;
        this.registeredAt = registeredAt;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }
}
