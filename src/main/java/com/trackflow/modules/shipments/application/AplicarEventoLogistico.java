package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.Shipment;
import com.trackflow.modules.shipments.domain.ShipmentNotFoundException;
import com.trackflow.modules.shipments.domain.ShipmentStatus;
import com.trackflow.modules.shipments.domain.TrackingNumber;
import com.trackflow.shared.events.EventoLogisticoRegistradoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AplicarEventoLogistico {

    private static final Logger log = LoggerFactory.getLogger(AplicarEventoLogistico.class);

    private final ShipmentRepository shipments;

    public AplicarEventoLogistico(ShipmentRepository shipments) {
        this.shipments = shipments;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ejecutar(EventoLogisticoRegistradoEvent event) {
        TrackingNumber trackingNumber = TrackingNumber.of(event.trackingNumber());
        Shipment shipment = shipments.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException(event.trackingNumber()));

        boolean aplicado = shipment.aplicarMovimiento(
                ShipmentStatus.valueOf(event.resultingStatus()),
                event.point(),
                event.movedAt());

        if (!aplicado) {
            log.info("Movimiento {} del envío {} es anterior al último aplicado; no cambia el estado",
                    event.logisticsEventId(), event.trackingNumber());
            return;
        }

        shipments.save(shipment);
    }
}
