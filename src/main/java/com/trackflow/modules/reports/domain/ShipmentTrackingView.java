package com.trackflow.modules.reports.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Modelo de lectura del seguimiento, construido con los eventos de shipments y logistics.
 */
@Entity
@Table(name = "reports_shipment_tracking")
public class ShipmentTrackingView {

    @Id
    private String trackingNumber;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String destinationCity;

    @Column(nullable = false)
    private Instant registeredAt;

    private String lastMovementPoint;

    private Instant lastMovementAt;

    protected ShipmentTrackingView() {
    }

    public ShipmentTrackingView(String trackingNumber, String status, String recipientName, String destinationCity,
            Instant registeredAt) {
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.recipientName = recipientName;
        this.destinationCity = destinationCity;
        this.registeredAt = registeredAt;
    }

    public void aplicarMovimiento(String status, String point, Instant movedAt) {
        this.status = status;
        this.lastMovementPoint = point;
        this.lastMovementAt = movedAt;
    }

    public boolean tieneMovimientos() {
        return lastMovementAt != null;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public String getLastMovementPoint() {
        return lastMovementPoint;
    }

    public Instant getLastMovementAt() {
        return lastMovementAt;
    }
}
