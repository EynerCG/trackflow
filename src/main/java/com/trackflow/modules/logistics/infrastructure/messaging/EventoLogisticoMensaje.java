package com.trackflow.modules.logistics.infrastructure.messaging;

import com.trackflow.modules.logistics.application.EventoLogisticoEntrante;
import com.trackflow.modules.logistics.domain.EventType;
import java.time.Instant;

/**
 * Contrato del mensaje que viaja por RabbitMQ. Es la forma pública que consumen
 * los puntos de la cadena, separada del modelo interno de la aplicación.
 */
public record EventoLogisticoMensaje(
        String eventId,
        String trackingNumber,
        String tipo,
        String punto,
        String observaciones,
        Instant ocurridoEn) {

    public static EventoLogisticoMensaje from(EventoLogisticoEntrante evento) {
        return new EventoLogisticoMensaje(
                evento.eventId(),
                evento.trackingNumber(),
                evento.tipo().name(),
                evento.punto(),
                evento.observaciones(),
                evento.ocurridoEn());
    }

    public EventoLogisticoEntrante toEntrante() {
        return new EventoLogisticoEntrante(
                eventId,
                trackingNumber,
                EventType.valueOf(tipo),
                punto,
                observaciones,
                ocurridoEn);
    }
}
