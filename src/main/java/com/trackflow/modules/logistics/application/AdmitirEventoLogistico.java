package com.trackflow.modules.logistics.application;

import com.trackflow.modules.logistics.domain.EventType;
import com.trackflow.modules.logistics.domain.UnknownShipmentException;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Admite un evento reportado por un punto de la cadena y lo encola.
 * No lo registra: de eso se encarga {@link RegistrarEventoLogistico} al consumirlo.
 */
@Service
public class AdmitirEventoLogistico {

    public record Command(String trackingNumber, EventType tipo, String punto, String observaciones) {
    }

    private final TrackedShipmentRepository trackedShipments;
    private final EventoLogisticoPublisher publisher;
    private final Clock clock;

    public AdmitirEventoLogistico(TrackedShipmentRepository trackedShipments, EventoLogisticoPublisher publisher,
            Clock clock) {
        this.trackedShipments = trackedShipments;
        this.publisher = publisher;
        this.clock = clock;
    }

    public EventoLogisticoEntrante ejecutar(Command command) {
        if (!trackedShipments.exists(command.trackingNumber())) {
            throw new UnknownShipmentException(command.trackingNumber());
        }

        EventoLogisticoEntrante evento = new EventoLogisticoEntrante(
                UUID.randomUUID().toString(),
                command.trackingNumber(),
                command.tipo(),
                command.punto(),
                command.observaciones(),
                clock.instant());

        publisher.publicar(evento);

        return evento;
    }
}
