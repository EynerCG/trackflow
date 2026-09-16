package com.trackflow.modules.logistics.application;

import com.trackflow.modules.logistics.domain.EventType;
import com.trackflow.modules.logistics.domain.LogisticsEvent;
import com.trackflow.modules.logistics.domain.UnknownShipmentException;
import com.trackflow.shared.events.EventPublisher;
import com.trackflow.shared.events.EventoLogisticoRegistradoEvent;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarEventoLogistico {

    public record Command(String trackingNumber, EventType tipo, String punto, String observaciones) {
    }

    private final LogisticsEventRepository logisticsEvents;
    private final TrackedShipmentRepository trackedShipments;
    private final EventPublisher events;
    private final Clock clock;

    public RegistrarEventoLogistico(LogisticsEventRepository logisticsEvents,
            TrackedShipmentRepository trackedShipments, EventPublisher events, Clock clock) {
        this.logisticsEvents = logisticsEvents;
        this.trackedShipments = trackedShipments;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public LogisticsEvent ejecutar(Command command) {
        if (!trackedShipments.exists(command.trackingNumber())) {
            throw new UnknownShipmentException(command.trackingNumber());
        }

        Instant now = clock.instant();
        LogisticsEvent saved = logisticsEvents.save(LogisticsEvent.registrar(
                command.trackingNumber(),
                command.tipo(),
                command.punto(),
                command.observaciones(),
                now));

        events.publish(new EventoLogisticoRegistradoEvent(
                saved.getId(),
                saved.getTrackingNumber(),
                saved.getType().name(),
                saved.getType().resultingStatus(),
                saved.getPoint(),
                saved.getRegisteredAt(),
                now));

        return saved;
    }
}
