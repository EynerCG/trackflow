package com.trackflow.modules.logistics.infrastructure.messaging;

import com.trackflow.modules.logistics.application.RegistrarEventoLogistico;
import com.trackflow.modules.logistics.domain.UnknownShipmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada por mensajería: recibe lo que reportan los puntos de la cadena
 * y lo entrega al mismo caso de uso que usa el adaptador REST.
 */
@Component
public class RabbitMQEventoLogisticoListener {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventoLogisticoListener.class);

    private final RegistrarEventoLogistico registrarEventoLogistico;

    public RabbitMQEventoLogisticoListener(RegistrarEventoLogistico registrarEventoLogistico) {
        this.registrarEventoLogistico = registrarEventoLogistico;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void recibir(EventoLogisticoMensaje mensaje) {
        try {
            registrarEventoLogistico.ejecutar(mensaje.toEntrante());
        } catch (UnknownShipmentException e) {
            // Reintentar no va a hacer que el envío exista: a la cola de descartados.
            log.warn("Evento {} descartado: {}", mensaje.eventId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
