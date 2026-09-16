package com.trackflow.modules.shipments.infrastructure.messaging;

import com.trackflow.modules.shipments.application.EnvioSolicitado;
import com.trackflow.modules.shipments.application.EnvioSolicitadoPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQEnvioSolicitadoPublisher implements EnvioSolicitadoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEnvioSolicitadoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(EnvioSolicitado solicitud) {
        rabbitTemplate.convertAndSend(
                ShipmentsRabbitMQConfig.EXCHANGE,
                ShipmentsRabbitMQConfig.ROUTING_KEY,
                EnvioSolicitadoMensaje.from(solicitud));
    }
}
