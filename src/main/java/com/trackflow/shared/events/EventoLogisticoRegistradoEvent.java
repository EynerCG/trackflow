package com.trackflow.shared.events;

import java.time.Instant;

/**
 * Evento de integración: lo publica logistics y lo consumen shipments y reports.
 * Vive en shared porque es el contrato entre módulos, no el modelo interno de ninguno.
 */
public record EventoLogisticoRegistradoEvent(
        Long logisticsEventId,
        String trackingNumber,
        String eventType,
        String resultingStatus,
        String point,
        Instant registeredAt,
        Instant occurredAt) implements DomainEvent {
}
