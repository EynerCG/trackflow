package com.trackflow.modules.shipments.api.dto;

import com.trackflow.modules.shipments.domain.Shipment;
import java.time.Instant;

public record EnvioRegistradoResponse(
        String trackingNumber,
        String status,
        String remitente,
        String destinatario,
        String ciudadDestino,
        Instant registeredAt) {

    public static EnvioRegistradoResponse from(Shipment shipment) {
        return new EnvioRegistradoResponse(
                shipment.getTrackingNumber().value(),
                shipment.getStatus().name(),
                shipment.getSender().getFullName(),
                shipment.getRecipient().getFullName(),
                shipment.getRecipient().getCity(),
                shipment.getRegisteredAt());
    }
}
