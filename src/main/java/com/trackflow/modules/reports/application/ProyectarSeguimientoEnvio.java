package com.trackflow.modules.reports.application;

import com.trackflow.modules.reports.domain.ShipmentTrackingView;
import com.trackflow.shared.events.EnvioCreadoEvent;
import com.trackflow.shared.events.EventoLogisticoRegistradoEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProyectarSeguimientoEnvio {

    private final ShipmentTrackingViewRepository views;

    public ProyectarSeguimientoEnvio(ShipmentTrackingViewRepository views) {
        this.views = views;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alCrearseElEnvio(EnvioCreadoEvent event) {
        views.save(new ShipmentTrackingView(
                event.trackingNumber(),
                event.status(),
                event.senderName(),
                event.originCityId(),
                event.originCity(),
                event.recipientName(),
                event.recipientAddress(),
                event.destinationCityId(),
                event.destinationCity(),
                event.registeredAt()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alRegistrarseUnEvento(EventoLogisticoRegistradoEvent event) {
        views.findByTrackingNumber(event.trackingNumber()).ifPresent(view -> {
            if (view.aplicarMovimiento(event.resultingStatus(), event.point(), event.movedAt())) {
                views.save(view);
            }
        });
    }
}
