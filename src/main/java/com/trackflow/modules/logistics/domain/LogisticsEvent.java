package com.trackflow.modules.logistics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "logistics_events")
public class LogisticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EventType type;

    @Column(nullable = false, updatable = false)
    private String point;

    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant registeredAt;

    protected LogisticsEvent() {
    }

    private LogisticsEvent(String trackingNumber, EventType type, String point, String notes, Instant registeredAt) {
        this.trackingNumber = trackingNumber;
        this.type = type;
        this.point = point;
        this.notes = notes;
        this.registeredAt = registeredAt;
    }

    public static LogisticsEvent registrar(String trackingNumber, EventType type, String point, String notes,
            Instant registeredAt) {
        return new LogisticsEvent(trackingNumber, type, point, notes, registeredAt);
    }

    public Long getId() {
        return id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public EventType getType() {
        return type;
    }

    public String getPoint() {
        return point;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }
}
