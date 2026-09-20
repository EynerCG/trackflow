package com.trackflow.modules.logistics.api.dto;

import com.trackflow.modules.logistics.application.EventoLogisticoEntrante;
import java.time.Instant;

public record EventoAdmitidoResponse(
        String eventId,
        String trackingNumber,
        String tipo,
        Long centroId,
        String punto,
        Instant ocurridoEn,
        String estadoProcesamiento) {

    public static EventoAdmitidoResponse from(EventoLogisticoEntrante evento) {
        return new EventoAdmitidoResponse(
                evento.eventId(),
                evento.trackingNumber(),
                evento.tipo().name(),
                evento.centroId(),
                evento.punto(),
                evento.ocurridoEn(),
                "ENCOLADO");
    }
}
