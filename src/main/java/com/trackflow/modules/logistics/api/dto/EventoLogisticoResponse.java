package com.trackflow.modules.logistics.api.dto;

import com.trackflow.modules.logistics.domain.LogisticsEvent;
import java.time.Instant;

public record EventoLogisticoResponse(
        Long id,
        String trackingNumber,
        String tipo,
        String estadoResultante,
        String punto,
        String observaciones,
        Instant ocurridoEn,
        Instant registradoEn) {

    public static EventoLogisticoResponse from(LogisticsEvent event) {
        return new EventoLogisticoResponse(
                event.getId(),
                event.getTrackingNumber(),
                event.getType().name(),
                event.getType().resultingStatus(),
                event.getPoint(),
                event.getNotes(),
                event.getOccurredAt(),
                event.getRegisteredAt());
    }
}
