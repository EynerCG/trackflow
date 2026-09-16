package com.trackflow.modules.shipments.application;

/**
 * Puerto de salida hacia el broker. Aísla a la capa de aplicación de RabbitMQ.
 */
public interface EnvioSolicitadoPublisher {

    void publicar(EnvioSolicitado solicitud);
}
