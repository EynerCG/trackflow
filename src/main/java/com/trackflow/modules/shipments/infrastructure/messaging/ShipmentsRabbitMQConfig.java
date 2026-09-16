package com.trackflow.modules.shipments.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShipmentsRabbitMQConfig {

    public static final String EXCHANGE = "shipments.topic";
    public static final String ROUTING_KEY = "shipment.requested";
    public static final String QUEUE = "shipments.requests.queue";

    public static final String DEAD_LETTER_EXCHANGE = "shipments.dlx";
    public static final String DEAD_LETTER_QUEUE = "shipments.requests.dlq";

    @Bean
    TopicExchange shipmentsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    TopicExchange shipmentsDeadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    Queue shipmentsRequestsQueue() {
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_KEY)
                .build();
    }

    @Bean
    Queue shipmentsDeadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding shipmentsRequestsBinding() {
        return BindingBuilder.bind(shipmentsRequestsQueue()).to(shipmentsExchange()).with(ROUTING_KEY);
    }

    @Bean
    Binding shipmentsDeadLetterBinding() {
        return BindingBuilder.bind(shipmentsDeadLetterQueue()).to(shipmentsDeadLetterExchange()).with(ROUTING_KEY);
    }
}
