package com.trackflow.modules.logistics.application;

import com.trackflow.modules.logistics.domain.EventType;
import java.time.Instant;

/**
 * Evento logístico tal como lo reporta un punto de la cadena, antes de ser registrado.
 * Es el contrato de ingesta: lo produce el adaptador REST y lo consume el de mensajería.
 */
public record EventoLogisticoEntrante(
        String eventId,
        String trackingNumber,
        EventType tipo,
        String punto,
        String observaciones,
        Instant ocurridoEn) {
}
