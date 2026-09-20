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
    private String senderName;

    /** Identificador de la ciudad en el catálogo, para agrupar por origen. */
    @Column(nullable = false)
    private Long originCityId;

    /** Misma duplicación intencional que destinationCity, para el origen. */
    @Column(nullable = false)
    private String originCity;

    @Column(nullable = false)
    private String recipientName;

    /** Identificador de la ciudad en el catálogo, para agrupar por destino. */
    @Column(nullable = false)
    private Long destinationCityId;

    /**
     * La etiqueta que ve el cliente ("MEDELLÍN - ANTIOQUIA"). Aquí la duplicación es
     * intencional: es lo que permite responder la consulta sin unir tablas.
     */
    @Column(nullable = false)
    private String destinationCity;

    @Column(nullable = false)
    private Instant registeredAt;

    private String lastMovementPoint;

    private Instant lastMovementAt;

    protected ShipmentTrackingView() {
    }

    public ShipmentTrackingView(String trackingNumber, String status, String senderName, Long originCityId,
            String originCity, String recipientName, Long destinationCityId, String destinationCity,
            Instant registeredAt) {
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.senderName = senderName;
        this.originCityId = originCityId;
        this.originCity = originCity;
        this.recipientName = recipientName;
        this.destinationCityId = destinationCityId;
        this.destinationCity = destinationCity;
        this.registeredAt = registeredAt;
    }

    /**
     * Ignora los movimientos anteriores al último aplicado, por la misma razón que el
     * agregado: los reportes no llegan necesariamente en orden y un movimiento
     * rezagado no debe hacer retroceder el estado que ve el cliente.
     *
     * @return false si el movimiento es anterior al último aplicado y se ignora
     */
    public boolean aplicarMovimiento(String status, String point, Instant movedAt) {
        if (lastMovementAt != null && movedAt.isBefore(lastMovementAt)) {
            return false;
        }

        this.status = status;
        this.lastMovementPoint = point;
        this.lastMovementAt = movedAt;

        return true;
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

    public String getSenderName() {
        return senderName;
    }

    public Long getOriginCityId() {
        return originCityId;
    }

    public String getOriginCity() {
        return originCity;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public Long getDestinationCityId() {
        return destinationCityId;
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
