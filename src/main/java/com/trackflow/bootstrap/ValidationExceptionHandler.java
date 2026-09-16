package com.trackflow.bootstrap;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Detalla qué campos obligatorios faltan, como piden los criterios de aceptación.
 * La respuesta por defecto solo dice "Invalid request content", que no le sirve
 * a quien está registrando el envío.
 *
 * Es transversal a todos los módulos y no depende de ninguno: solo traduce los
 * errores de Bean Validation.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail camposInvalidos(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problema.setTitle("Datos obligatorios incompletos");
        problema.setDetail("La solicitud no se puede procesar porque faltan datos obligatorios");
        problema.setProperty("camposFaltantes", campos);

        return problema;
    }
}
