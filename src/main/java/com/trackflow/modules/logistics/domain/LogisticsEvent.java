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

    /** Identificador del mensaje de ingesta. Unico para descartar reentregas del broker. */
    @Column(nullable = false, unique = true, updatable = false)
    private String eventId;

    @Column(nullable = false, updatable = false)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EventType type;

    @Column(nullable = false, updatable = false)
    private String point;

    private String notes;

    /**
     * Id del centro del catálogo, si el evento se reportó con uno. Nullable a
     * propósito: eventos históricos y los que usan el respaldo de texto libre no
     * tienen centro asociado. No se resuelve como referencia viva — si el centro se
     * renombra o desactiva después, este evento no cambia.
     */
    @Column(updatable = false)
    private Long centerId;

    /** Nombre de la ciudad del centro, congelado en el momento del registro. */
    @Column(updatable = false)
    private String cityName;

    /** Cuándo ocurrió el movimiento. Es la fecha que ve el cliente y la que ordena el historial. */
    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    /** Cuándo se recibió el reporte. Sirve para auditar cuánto tardó en reportarse. */
    @Column(nullable = false, updatable = false)
    private Instant registeredAt;

    protected LogisticsEvent() {
    }

    private LogisticsEvent(String eventId, String trackingNumber, EventType type, String point, String notes,
            Long centerId, String cityName, Instant occurredAt, Instant registeredAt) {
        this.eventId = eventId;
        this.trackingNumber = trackingNumber;
        this.type = type;
        this.point = point;
        this.notes = notes;
        this.centerId = centerId;
        this.cityName = cityName;
        this.occurredAt = occurredAt;
        this.registeredAt = registeredAt;
    }

    /**
     * Un movimiento no puede haber ocurrido después de reportarse. Se comprueba aquí
     * y no en el DTO porque el reporte llega por REST y también por la cola.
     */
    public static LogisticsEvent registrar(String eventId, String trackingNumber, EventType type, String point,
            String notes, Long centerId, String cityName, Instant occurredAt, Instant registeredAt) {
        if (occurredAt == null || occurredAt.isAfter(registeredAt)) {
            throw new FechaDeMovimientoInvalidaException(occurredAt, registeredAt);
        }

        return new LogisticsEvent(eventId, trackingNumber, type, point, notes, centerId, cityName, occurredAt,
                registeredAt);
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
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

    public Long getCenterId() {
        return centerId;
    }

    public String getCityName() {
        return cityName;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }
}
