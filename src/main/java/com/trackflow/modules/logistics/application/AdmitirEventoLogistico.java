package com.trackflow.modules.logistics.application;

import com.trackflow.modules.logistics.domain.EventType;
import com.trackflow.modules.logistics.domain.FechaDeMovimientoInvalidaException;
import com.trackflow.modules.logistics.domain.UnknownShipmentException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Admite un evento reportado por un punto de la cadena y lo encola.
 * No lo registra: de eso se encarga {@link RegistrarEventoLogistico} al consumirlo.
 */
@Service
public class AdmitirEventoLogistico {

    /**
     * {@code ocurridoEn} es opcional: si el punto de la cadena no lo reporta, se
     * asume que el movimiento acaba de ocurrir.
     */
    public record Command(String trackingNumber, EventType tipo, String punto, String observaciones,
            Instant ocurridoEn) {
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

        Instant ahora = clock.instant();
        Instant ocurridoEn = command.ocurridoEn() == null ? ahora : command.ocurridoEn();

        if (ocurridoEn.isAfter(ahora)) {
            throw new FechaDeMovimientoInvalidaException(ocurridoEn, ahora);
        }

        EventoLogisticoEntrante evento = new EventoLogisticoEntrante(
                UUID.randomUUID().toString(),
                command.trackingNumber(),
                command.tipo(),
                command.punto(),
                command.observaciones(),
                ocurridoEn);

        publisher.publicar(evento);

        return evento;
    }
}
