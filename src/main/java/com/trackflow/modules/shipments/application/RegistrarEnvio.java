package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.Party;
import com.trackflow.modules.shipments.domain.Shipment;
import com.trackflow.shared.events.EnvioCreadoEvent;
import com.trackflow.shared.events.EventPublisher;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarEnvio {

    public record Command(Party remitente, Party destinatario, String descripcion) {
    }

    private final ShipmentRepository shipments;
    private final TrackingNumberGenerator trackingNumbers;
    private final EventPublisher events;
    private final Clock clock;

    public RegistrarEnvio(ShipmentRepository shipments, TrackingNumberGenerator trackingNumbers, EventPublisher events,
            Clock clock) {
        this.shipments = shipments;
        this.trackingNumbers = trackingNumbers;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public Shipment ejecutar(Command command) {
        Instant now = clock.instant();
        Shipment shipment = Shipment.registrar(
                trackingNumbers.next(),
                command.remitente(),
                command.destinatario(),
                command.descripcion(),
                now);

        Shipment saved = shipments.save(shipment);

        events.publish(new EnvioCreadoEvent(
                saved.getTrackingNumber().value(),
                saved.getStatus().name(),
                saved.getRecipient().getFullName(),
                saved.getRecipient().getCity(),
                saved.getRegisteredAt(),
                now));

        return saved;
    }
}
