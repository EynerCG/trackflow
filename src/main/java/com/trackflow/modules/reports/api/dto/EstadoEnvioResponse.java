package com.trackflow.modules.reports.api.dto;

import com.trackflow.modules.reports.domain.ShipmentTrackingView;
import java.time.Instant;

public record EstadoEnvioResponse(
        String trackingNumber,
        String estado,
        String remitenteNombre,
        String ciudadOrigen,
        String destinatarioNombre,
        Long ciudadDestinoId,
        String ciudadDestino,
        Instant registeredAt,
        boolean tieneMovimientos,
        String ultimoPunto,
        Instant ultimoMovimientoAt,

        /**
         * Dirección del destinatario. Solo viene informada cuando el envío ya está
         * ENTREGADO — antes de eso el paquete no ha llegado ahí, así que mostrarla no
         * tiene sentido; en cualquier otro estado este campo es null.
         */
        String direccionDestino) {

    public static EstadoEnvioResponse from(ShipmentTrackingView view) {
        return new EstadoEnvioResponse(
                view.getTrackingNumber(),
                view.getStatus(),
                view.getSenderName(),
                view.getOriginCity(),
                view.getRecipientName(),
                view.getDestinationCityId(),
                view.getDestinationCity(),
                view.getRegisteredAt(),
                view.tieneMovimientos(),
                view.getLastMovementPoint(),
                view.getLastMovementAt(),
                // Mismo literal que ShipmentStatus.DELIVERED.name() en shipments; no se puede
                // importar el enum desde reports, así que se compara como texto — igual que ya
                // hace EventType.resultingStatus() al cruzar el mismo límite entre módulos.
                "DELIVERED".equals(view.getStatus()) ? view.getRecipientAddress() : null);
    }
}
