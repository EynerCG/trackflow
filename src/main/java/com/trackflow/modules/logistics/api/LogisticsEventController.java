package com.trackflow.modules.logistics.api;

import com.trackflow.modules.logistics.api.dto.EventoLogisticoResponse;
import com.trackflow.modules.logistics.api.dto.RegistrarEventoRequest;
import com.trackflow.modules.logistics.application.LogisticsEventRepository;
import com.trackflow.modules.logistics.application.RegistrarEventoLogistico;
import com.trackflow.modules.logistics.domain.LogisticsEvent;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipments/{trackingNumber}/events")
public class LogisticsEventController {

    private final RegistrarEventoLogistico registrarEventoLogistico;
    private final LogisticsEventRepository logisticsEvents;

    public LogisticsEventController(RegistrarEventoLogistico registrarEventoLogistico,
            LogisticsEventRepository logisticsEvents) {
        this.registrarEventoLogistico = registrarEventoLogistico;
        this.logisticsEvents = logisticsEvents;
    }

    @PostMapping
    public ResponseEntity<EventoLogisticoResponse> registrar(@PathVariable String trackingNumber,
            @Valid @RequestBody RegistrarEventoRequest request) {
        LogisticsEvent event = registrarEventoLogistico.ejecutar(new RegistrarEventoLogistico.Command(
                trackingNumber,
                request.tipo(),
                request.punto(),
                request.observaciones()));

        return ResponseEntity.status(HttpStatus.CREATED).body(EventoLogisticoResponse.from(event));
    }

    @GetMapping
    public List<EventoLogisticoResponse> historial(@PathVariable String trackingNumber) {
        return logisticsEvents.findHistorial(trackingNumber).stream()
                .map(EventoLogisticoResponse::from)
                .toList();
    }
}
