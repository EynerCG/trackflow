package com.trackflow.modules.reports.api.dto;

import com.trackflow.modules.reports.domain.ShipmentTrackingView;
import java.time.Instant;

public record EstadoEnvioResponse(
        String trackingNumber,
        String estado,
        Long ciudadDestinoId,
        String ciudadDestino,
        Instant registeredAt,
        boolean tieneMovimientos,
        String ultimoPunto,
        Instant ultimoMovimientoAt) {

    public static EstadoEnvioResponse from(ShipmentTrackingView view) {
        return new EstadoEnvioResponse(
                view.getTrackingNumber(),
                view.getStatus(),
                view.getDestinationCityId(),
                view.getDestinationCity(),
                view.getRegisteredAt(),
                view.tieneMovimientos(),
                view.getLastMovementPoint(),
                view.getLastMovementAt());
    }
}
