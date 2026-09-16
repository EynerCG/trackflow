package com.trackflow.modules.shipments.infrastructure.messaging;

import com.trackflow.modules.shipments.application.EnvioSolicitado;
import com.trackflow.modules.shipments.domain.Party;
import java.time.Instant;

/**
 * Contrato del mensaje que viaja por RabbitMQ, independiente del modelo de dominio.
 */
public record EnvioSolicitadoMensaje(
        String eventId,
        String trackingNumber,
        PersonaMensaje remitente,
        PersonaMensaje destinatario,
        String descripcion,
        Instant solicitadoEn) {

    public record PersonaMensaje(
            String nombreCompleto,
            String documento,
            String telefono,
            String direccion,
            String ciudad) {

        static PersonaMensaje from(Party party) {
            return new PersonaMensaje(
                    party.getFullName(),
                    party.getDocumentId(),
                    party.getPhone(),
                    party.getAddress(),
                    party.getCity());
        }

        Party toDomain() {
            return new Party(nombreCompleto, documento, telefono, direccion, ciudad);
        }
    }

    public static EnvioSolicitadoMensaje from(EnvioSolicitado solicitud) {
        return new EnvioSolicitadoMensaje(
                solicitud.eventId(),
                solicitud.trackingNumber(),
                PersonaMensaje.from(solicitud.remitente()),
                PersonaMensaje.from(solicitud.destinatario()),
                solicitud.descripcion(),
                solicitud.solicitadoEn());
    }

    public EnvioSolicitado toSolicitud() {
        return new EnvioSolicitado(
                eventId,
                trackingNumber,
                remitente.toDomain(),
                destinatario.toDomain(),
                descripcion,
                solicitadoEn);
    }
}
