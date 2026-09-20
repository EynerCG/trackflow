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
        Long centroId,
        String punto,
        String ciudadNombre,
        String observaciones,
        Instant ocurridoEn) {

    public static EventoLogisticoMensaje from(EventoLogisticoEntrante evento) {
        return new EventoLogisticoMensaje(
                evento.eventId(),
                evento.trackingNumber(),
                evento.tipo().name(),
                evento.centroId(),
                evento.punto(),
                evento.ciudadNombre(),
                evento.observaciones(),
                evento.ocurridoEn());
    }

    public EventoLogisticoEntrante toEntrante() {
        return new EventoLogisticoEntrante(
                eventId,
                trackingNumber,
                EventType.valueOf(tipo),
                centroId,
                punto,
                ciudadNombre,
                observaciones,
                ocurridoEn);
    }
}
