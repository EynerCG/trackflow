package com.trackflow.modules.shipments.api.dto;

import com.trackflow.modules.shipments.application.EnvioSolicitado;
import java.time.Instant;

public record EnvioAdmitidoResponse(
        String trackingNumber,
        String eventId,
        String destinatario,
        String ciudadDestino,
        Instant solicitadoEn,
        String estadoProcesamiento) {

    public static EnvioAdmitidoResponse from(EnvioSolicitado solicitud) {
        return new EnvioAdmitidoResponse(
                solicitud.trackingNumber(),
                solicitud.eventId(),
                solicitud.destinatario().getFullName(),
                solicitud.destinatario().getCity(),
                solicitud.solicitadoEn(),
                "ENCOLADO");
    }
}
