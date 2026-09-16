package com.trackflow.modules.shipments.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private TrackingNumber trackingNumber;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fullName", column = @Column(name = "sender_full_name", nullable = false)),
            @AttributeOverride(name = "documentId", column = @Column(name = "sender_document_id", nullable = false)),
            @AttributeOverride(name = "phone", column = @Column(name = "sender_phone", nullable = false)),
            @AttributeOverride(name = "address", column = @Column(name = "sender_address", nullable = false)),
            @AttributeOverride(name = "city", column = @Column(name = "sender_city", nullable = false))
    })
    private Party sender;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fullName", column = @Column(name = "recipient_full_name", nullable = false)),
            @AttributeOverride(name = "documentId", column = @Column(name = "recipient_document_id", nullable = false)),
            @AttributeOverride(name = "phone", column = @Column(name = "recipient_phone", nullable = false)),
            @AttributeOverride(name = "address", column = @Column(name = "recipient_address", nullable = false)),
            @AttributeOverride(name = "city", column = @Column(name = "recipient_city", nullable = false))
    })
    private Party recipient;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(nullable = false, updatable = false)
    private Instant registeredAt;

    private String lastMovementPoint;

    private Instant lastMovementAt;

    protected Shipment() {
    }

    private Shipment(TrackingNumber trackingNumber, Party sender, Party recipient, String description,
            Instant registeredAt) {
        this.trackingNumber = trackingNumber;
        this.sender = sender;
        this.recipient = recipient;
        this.description = description;
        this.registeredAt = registeredAt;
        this.status = ShipmentStatus.REGISTERED;
    }

    public static Shipment registrar(TrackingNumber trackingNumber, Party sender, Party recipient, String description,
            Instant registeredAt) {
        return new Shipment(trackingNumber, sender, recipient, description, registeredAt);
    }

    public void aplicarMovimiento(ShipmentStatus resultingStatus, String point, Instant movedAt) {
        this.status = resultingStatus;
        this.lastMovementPoint = point;
        this.lastMovementAt = movedAt;
    }

    public Long getId() {
        return id;
    }

    public TrackingNumber getTrackingNumber() {
        return trackingNumber;
    }

    public Party getSender() {
        return sender;
    }

    public Party getRecipient() {
        return recipient;
    }

    public String getDescription() {
        return description;
    }

    public ShipmentStatus getStatus() {
        return status;
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
