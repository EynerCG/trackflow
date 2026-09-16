package com.trackflow.modules.reports.api;

import com.trackflow.modules.reports.domain.TrackingNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ConsultaEnvioController.class)
public class ReportsExceptionHandler {

    @ExceptionHandler(TrackingNotFoundException.class)
    ProblemDetail seguimientoNoEncontrado(TrackingNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
