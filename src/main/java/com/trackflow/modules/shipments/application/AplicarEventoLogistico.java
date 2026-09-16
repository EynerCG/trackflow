package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.Shipment;
import com.trackflow.modules.shipments.domain.ShipmentNotFoundException;
import com.trackflow.modules.shipments.domain.ShipmentStatus;
import com.trackflow.modules.shipments.domain.TrackingNumber;
import com.trackflow.shared.events.EventoLogisticoRegistradoEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AplicarEventoLogistico {

    private final ShipmentRepository shipments;

    public AplicarEventoLogistico(ShipmentRepository shipments) {
        this.shipments = shipments;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ejecutar(EventoLogisticoRegistradoEvent event) {
        TrackingNumber trackingNumber = TrackingNumber.of(event.trackingNumber());
        Shipment shipment = shipments.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException(event.trackingNumber()));

        shipment.aplicarMovimiento(
                ShipmentStatus.valueOf(event.resultingStatus()),
                event.point(),
                event.registeredAt());

        shipments.save(shipment);
    }
}
