package com.trackflow.modules.logistics.api;

import com.trackflow.modules.logistics.domain.UnknownShipmentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones del dominio a respuestas HTTP. Vive en api/ para que el
 * dominio no tenga que conocer el protocolo.
 */
@RestControllerAdvice(assignableTypes = LogisticsEventController.class)
public class LogisticsExceptionHandler {

    @ExceptionHandler(UnknownShipmentException.class)
    ProblemDetail envioDesconocido(UnknownShipmentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
