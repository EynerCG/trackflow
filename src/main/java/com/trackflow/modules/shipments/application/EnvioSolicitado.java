package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.Party;
import com.trackflow.shared.geografia.Ciudad;
import java.time.Instant;

/**
 * Solicitud de registro admitida y pendiente de procesar. Es el contrato de ingesta
 * de envíos: se publica al broker y se consume para registrar el envío.
 *
 * Lleva las ciudades de origen y destino ya resueltas, y no solo su identificador,
 * para que quien consuma el mensaje pueda construir la etiqueta que ve el cliente
 * sin volver a consultar el catálogo.
 */
public record EnvioSolicitado(
        String eventId,
        String trackingNumber,
        Party remitente,
        Party destinatario,
        Ciudad ciudadOrigen,
        Ciudad ciudadDestino,
        String descripcion,
        Instant solicitadoEn) {
}
