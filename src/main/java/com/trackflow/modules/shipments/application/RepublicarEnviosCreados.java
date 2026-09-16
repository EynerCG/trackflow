package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.Shipment;
import com.trackflow.shared.events.EnvioCreadoEvent;
import com.trackflow.shared.events.EventPublisher;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vuelve a publicar el alta de todos los envíos registrados, para que quien
 * mantenga proyecciones a partir de esos eventos pueda reconstruirlas.
 */
@Service
public class RepublicarEnviosCreados {

    private static final Logger log = LoggerFactory.getLogger(RepublicarEnviosCreados.class);

    private final ShipmentRepository shipments;
    private final EventPublisher events;
    private final Clock clock;

    public RepublicarEnviosCreados(ShipmentRepository shipments, EventPublisher events, Clock clock) {
        this.shipments = shipments;
        this.events = events;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public int ejecutar() {
        List<Shipment> todos = shipments.findAll();

        for (Shipment shipment : todos) {
            events.publish(new EnvioCreadoEvent(
                    shipment.getTrackingNumber().value(),
                    shipment.getStatus().name(),
                    shipment.getRecipient().getFullName(),
                    shipment.getRecipient().getCity(),
                    shipment.getRegisteredAt(),
                    clock.instant()));
        }

        log.info("Republicados {} envíos para reconstrucción", todos.size());
        return todos.size();
    }
}
