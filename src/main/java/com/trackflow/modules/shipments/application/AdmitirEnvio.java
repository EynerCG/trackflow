package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.Party;
import com.trackflow.modules.shipments.domain.TrackingNumber;
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

    public record Command(Party remitente, Party destinatario, String descripcion) {
    }

    private final TrackingNumberGenerator trackingNumbers;
    private final EnvioSolicitadoPublisher publisher;
    private final Clock clock;

    public AdmitirEnvio(TrackingNumberGenerator trackingNumbers, EnvioSolicitadoPublisher publisher, Clock clock) {
        this.trackingNumbers = trackingNumbers;
        this.publisher = publisher;
        this.clock = clock;
    }

    public EnvioSolicitado ejecutar(Command command) {
        TrackingNumber trackingNumber = trackingNumbers.next();

        EnvioSolicitado solicitud = new EnvioSolicitado(
                UUID.randomUUID().toString(),
                trackingNumber.value(),
                command.remitente(),
                command.destinatario(),
                command.descripcion(),
                clock.instant());

        publisher.publicar(solicitud);

        return solicitud;
    }
}
