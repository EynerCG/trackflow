package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.Party;
import com.trackflow.modules.shipments.domain.TrackingNumber;
import com.trackflow.shared.geografia.CatalogoDeCiudades;
import com.trackflow.shared.geografia.Ciudad;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Admite la solicitud de registro y la encola. El número de seguimiento se genera aquí
 * para poder entregárselo al remitente de inmediato, como pide HU-01; el registro
 * en sí lo hace {@link RegistrarEnvio} al consumir el mensaje.
 */
@Service
public class AdmitirEnvio {

    public record Command(DatosPersona remitente, DatosPersona destinatario, String descripcion) {
    }

    private final TrackingNumberGenerator trackingNumbers;
    private final EnvioSolicitadoPublisher publisher;
    private final CatalogoDeCiudades ciudades;
    private final Clock clock;

    public AdmitirEnvio(TrackingNumberGenerator trackingNumbers, EnvioSolicitadoPublisher publisher,
            CatalogoDeCiudades ciudades, Clock clock) {
        this.trackingNumbers = trackingNumbers;
        this.publisher = publisher;
        this.ciudades = ciudades;
        this.clock = clock;
    }

    /**
     * Las ciudades se resuelven aquí, antes de encolar: una ciudad inexistente se
     * rechaza de inmediato con un 400 en lugar de acabar en la cola de descartados.
     */
    public EnvioSolicitado ejecutar(Command command) {
        Ciudad ciudadRemitente = ciudades.exigir(command.remitente().ciudadId());
        Ciudad ciudadDestino = ciudades.exigir(command.destinatario().ciudadId());

        TrackingNumber trackingNumber = trackingNumbers.next();

        EnvioSolicitado solicitud = new EnvioSolicitado(
                UUID.randomUUID().toString(),
                trackingNumber.value(),
                aParty(command.remitente(), ciudadRemitente),
                aParty(command.destinatario(), ciudadDestino),
                ciudadDestino,
                command.descripcion(),
                clock.instant());

        publisher.publicar(solicitud);

        return solicitud;
    }

    private Party aParty(DatosPersona datos, Ciudad ciudad) {
        return new Party(
                datos.nombreCompleto(),
                datos.tipoDocumento(),
                datos.numeroDocumento(),
                datos.telefono(),
                datos.direccion(),
                ciudad.id());
    }
}
