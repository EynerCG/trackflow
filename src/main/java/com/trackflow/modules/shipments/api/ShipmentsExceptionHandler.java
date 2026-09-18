package com.trackflow.modules.shipments.api;

import com.trackflow.modules.shipments.domain.DocumentoInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones del dominio a respuestas HTTP. Vive en api/ para que el
 * dominio no tenga que conocer el protocolo.
 */
@RestControllerAdvice(assignableTypes = ShipmentController.class)
public class ShipmentsExceptionHandler {

    @ExceptionHandler(DocumentoInvalidoException.class)
    ProblemDetail documentoInvalido(DocumentoInvalidoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problema.setTitle("Documento de identidad inválido");
        return problema;
    }
}
