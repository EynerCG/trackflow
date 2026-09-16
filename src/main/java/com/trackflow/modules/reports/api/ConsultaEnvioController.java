package com.trackflow.modules.reports.api;

import com.trackflow.modules.reports.api.dto.EstadoEnvioResponse;
import com.trackflow.modules.reports.application.ConsultarEstadoEnvio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracking")
public class ConsultaEnvioController {

    private final ConsultarEstadoEnvio consultarEstadoEnvio;

    public ConsultaEnvioController(ConsultarEstadoEnvio consultarEstadoEnvio) {
        this.consultarEstadoEnvio = consultarEstadoEnvio;
    }

    @GetMapping("/{trackingNumber}")
    public EstadoEnvioResponse consultar(@PathVariable String trackingNumber) {
        return EstadoEnvioResponse.from(consultarEstadoEnvio.ejecutar(trackingNumber));
    }
}
