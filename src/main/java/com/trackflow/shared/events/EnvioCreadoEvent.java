package com.trackflow.shared.events;

import java.time.Instant;

/**
 * Evento de integración: lo publica shipments y lo consumen logistics y reports.
 * Vive en shared porque es el contrato entre módulos, no el modelo interno de ninguno.
 */
public record EnvioCreadoEvent(
        String trackingNumber,
        String status,
        String recipientName,
        String destinationCity,
        Instant registeredAt,
        Instant occurredAt) implements DomainEvent {
}
