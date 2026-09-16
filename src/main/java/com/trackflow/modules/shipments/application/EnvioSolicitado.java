package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.Party;
import java.time.Instant;

/**
 * Solicitud de registro admitida y pendiente de procesar. Es el contrato de ingesta
 * de envíos: se publica al broker y se consume para registrar el envío.
 */
public record EnvioSolicitado(
        String eventId,
        String trackingNumber,
        Party remitente,
        Party destinatario,
        String descripcion,
        Instant solicitadoEn) {
}
