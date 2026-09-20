package com.trackflow.modules.logistics.application;

import com.trackflow.modules.logistics.domain.Centro;
import com.trackflow.modules.logistics.domain.CentroFueraDeCiudadException;
import com.trackflow.modules.logistics.domain.EntregaSinRepartoPrevioException;
import com.trackflow.modules.logistics.domain.EventType;
import com.trackflow.modules.logistics.domain.FechaDeMovimientoInvalidaException;
import com.trackflow.modules.logistics.domain.LogisticsEvent;
import com.trackflow.modules.logistics.domain.TrackedShipment;
import com.trackflow.modules.logistics.domain.UnknownShipmentException;
import com.trackflow.shared.geografia.CatalogoDeCiudades;
import com.trackflow.shared.geografia.Ciudad;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Admite un evento reportado por un punto de la cadena y lo encola.
 * No lo registra: de eso se encarga {@link RegistrarEventoLogistico} al consumirlo.
 */
@Service
public class AdmitirEventoLogistico {

    /**
     * {@code centroId} es el camino preferido: resuelve el punto contra el catálogo y
     * habilita las reglas de coherencia de ciudad. {@code punto} es el respaldo de
     * texto libre, deprecado — se acepta durante la transición, pero sin él no hay
     * forma de saber en qué ciudad ocurrió el movimiento, así que las reglas de
     * coherencia no aplican en ese caso. {@code ocurridoEn} es opcional: si el punto
     * de la cadena no lo reporta, se asume que el movimiento acaba de ocurrir.
     */
    public record Command(String trackingNumber, EventType tipo, Long centroId, String punto, String observaciones,
            Instant ocurridoEn) {
    }

    private final TrackedShipmentRepository trackedShipments;
    private final LogisticsEventRepository logisticsEvents;
    private final CatalogoDeCentros centros;
    private final CatalogoDeCiudades ciudades;
    private final EventoLogisticoPublisher publisher;
    private final Clock clock;

    public AdmitirEventoLogistico(TrackedShipmentRepository trackedShipments,
            LogisticsEventRepository logisticsEvents, CatalogoDeCentros centros, CatalogoDeCiudades ciudades,
            EventoLogisticoPublisher publisher, Clock clock) {
        this.trackedShipments = trackedShipments;
        this.logisticsEvents = logisticsEvents;
        this.centros = centros;
        this.ciudades = ciudades;
        this.publisher = publisher;
        this.clock = clock;
    }

    public EventoLogisticoEntrante ejecutar(Command command) {
        TrackedShipment envio = trackedShipments.porTrackingNumber(command.trackingNumber())
                .orElseThrow(() -> new UnknownShipmentException(command.trackingNumber()));

        Instant ahora = clock.instant();
        Instant ocurridoEn = command.ocurridoEn() == null ? ahora : command.ocurridoEn();

        if (ocurridoEn.isAfter(ahora)) {
            throw new FechaDeMovimientoInvalidaException(ocurridoEn, ahora);
        }

        List<LogisticsEvent> historial = logisticsEvents.findHistorial(command.trackingNumber());

        String puntoNombre;
        String ciudadNombre;

        if (command.centroId() != null) {
            Centro centro = centros.exigir(command.centroId());
            Ciudad ciudadCentro = ciudades.exigir(centro.getCityId());

            validarCoherenciaDeCiudad(command.tipo(), centro, ciudadCentro, envio, historial);

            puntoNombre = centro.getName();
            ciudadNombre = ciudadCentro.etiqueta();
        } else {
            // Respaldo de texto libre: no hay ciudad que comparar, así que las
            // validaciones de coherencia de ciudad no aplican en este caso.
            puntoNombre = command.punto();
            ciudadNombre = null;
        }

        if (command.tipo() == EventType.DELIVERED && historial.stream()
                .noneMatch(evento -> evento.getType() == EventType.OUT_FOR_DELIVERY)) {
            throw new EntregaSinRepartoPrevioException(command.trackingNumber());
        }

        EventoLogisticoEntrante evento = new EventoLogisticoEntrante(
                UUID.randomUUID().toString(),
                command.trackingNumber(),
                command.tipo(),
                command.centroId(),
                puntoNombre,
                ciudadNombre,
                command.observaciones(),
                ocurridoEn);

        publisher.publicar(evento);

        return evento;
    }

    private void validarCoherenciaDeCiudad(EventType tipo, Centro centro, Ciudad ciudadCentro,
            TrackedShipment envio, List<LogisticsEvent> historial) {
        if (tipo == EventType.ARRIVED_AT_DESTINATION_CENTER
                && !centro.getCityId().equals(envio.getDestinationCityId())) {
            Ciudad ciudadDestino = ciudades.exigir(envio.getDestinationCityId());
            throw CentroFueraDeCiudadException.paraCentroDeDestino(
                    centro.getName(), ciudadCentro.etiqueta(), ciudadDestino.etiqueta());
        }

        if (tipo == EventType.RECEIVED_AT_CENTER && historial.isEmpty()
                && !centro.getCityId().equals(envio.getOriginCityId())) {
            Ciudad ciudadOrigen = ciudades.exigir(envio.getOriginCityId());
            throw CentroFueraDeCiudadException.paraCentroDeOrigen(
                    centro.getName(), ciudadCentro.etiqueta(), ciudadOrigen.etiqueta());
        }
    }
}
