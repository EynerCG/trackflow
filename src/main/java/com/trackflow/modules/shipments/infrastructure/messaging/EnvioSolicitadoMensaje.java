package com.trackflow.modules.shipments.infrastructure.messaging;

import com.trackflow.modules.shipments.application.EnvioSolicitado;
import com.trackflow.modules.shipments.domain.Party;
import com.trackflow.modules.shipments.domain.TipoDocumento;
import com.trackflow.shared.geografia.Ciudad;
import java.time.Instant;

/**
 * Contrato del mensaje que viaja por RabbitMQ, independiente del modelo de dominio.
 */
public record EnvioSolicitadoMensaje(
        String eventId,
        String trackingNumber,
        PersonaMensaje remitente,
        PersonaMensaje destinatario,
        CiudadMensaje ciudadDestino,
        String descripcion,
        Instant solicitadoEn) {

    public record PersonaMensaje(
            String nombreCompleto,
            String tipoDocumento,
            String numeroDocumento,
            String telefono,
            String direccion,
            Long ciudadId) {

        static PersonaMensaje from(Party party) {
            return new PersonaMensaje(
                    party.getFullName(),
                    party.getDocumentType().name(),
                    party.getDocumentNumber(),
                    party.getPhone(),
                    party.getAddress(),
                    party.getCityId());
        }

        Party toDomain() {
            return new Party(nombreCompleto, TipoDocumento.valueOf(tipoDocumento), numeroDocumento,
                    telefono, direccion, ciudadId);
        }
    }

    public record CiudadMensaje(Long id, String nombre, String departamento) {

        static CiudadMensaje from(Ciudad ciudad) {
            return new CiudadMensaje(ciudad.id(), ciudad.nombre(), ciudad.departamento());
        }

        Ciudad toDomain() {
            return new Ciudad(id, nombre, departamento);
        }
    }

    public static EnvioSolicitadoMensaje from(EnvioSolicitado solicitud) {
        return new EnvioSolicitadoMensaje(
                solicitud.eventId(),
                solicitud.trackingNumber(),
                PersonaMensaje.from(solicitud.remitente()),
                PersonaMensaje.from(solicitud.destinatario()),
                CiudadMensaje.from(solicitud.ciudadDestino()),
                solicitud.descripcion(),
                solicitud.solicitadoEn());
    }

    public EnvioSolicitado toSolicitud() {
        return new EnvioSolicitado(
                eventId,
                trackingNumber,
                remitente.toDomain(),
                destinatario.toDomain(),
                ciudadDestino.toDomain(),
                descripcion,
                solicitadoEn);
    }
}
