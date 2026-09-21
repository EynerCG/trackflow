package com.trackflow.modules.logistics.api.dto;

import com.trackflow.modules.logistics.domain.LogisticsEvent;
import java.time.Instant;

public record EventoLogisticoResponse(
        Long id,
        String trackingNumber,
        String tipo,
        String estadoResultante,
        Long centroId,
        String punto,
        String ciudadNombre,
        String observaciones,
        String repartidorNombre,
        Instant ocurridoEn,
        Instant registradoEn) {

    public static EventoLogisticoResponse from(LogisticsEvent event) {
        return new EventoLogisticoResponse(
                event.getId(),
                event.getTrackingNumber(),
                event.getType().name(),
                event.getType().resultingStatus(),
                event.getCenterId(),
                event.getPoint(),
                event.getCityName(),
                event.getNotes(),
                event.getDelivererName(),
                event.getOccurredAt(),
                event.getRegisteredAt());
    }
}
