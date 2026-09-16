package com.trackflow.modules.logistics.api;

import com.trackflow.modules.logistics.api.dto.EventoAdmitidoResponse;
import com.trackflow.modules.logistics.api.dto.EventoLogisticoResponse;
import com.trackflow.modules.logistics.api.dto.RegistrarEventoRequest;
import com.trackflow.modules.logistics.application.AdmitirEventoLogistico;
import com.trackflow.modules.logistics.application.ConsultarHistorial;
import com.trackflow.modules.logistics.application.EventoLogisticoEntrante;
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

/**
 * Adaptador de entrada REST. No registra el evento: lo admite y lo publica en el broker,
 * igual que haría cualquier punto de la cadena. Quien registra es el consumidor de la cola.
 */
@RestController
@RequestMapping("/api/shipments/{trackingNumber}/events")
public class LogisticsEventController {

    private final AdmitirEventoLogistico admitirEventoLogistico;
    private final ConsultarHistorial consultarHistorial;

    public LogisticsEventController(AdmitirEventoLogistico admitirEventoLogistico,
            ConsultarHistorial consultarHistorial) {
        this.admitirEventoLogistico = admitirEventoLogistico;
        this.consultarHistorial = consultarHistorial;
    }

    @PostMapping
    public ResponseEntity<EventoAdmitidoResponse> admitir(@PathVariable String trackingNumber,
            @Valid @RequestBody RegistrarEventoRequest request) {
        EventoLogisticoEntrante evento = admitirEventoLogistico.ejecutar(new AdmitirEventoLogistico.Command(
                trackingNumber,
                request.tipo(),
                request.punto(),
                request.observaciones()));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(EventoAdmitidoResponse.from(evento));
    }

    @GetMapping
    public List<EventoLogisticoResponse> historial(@PathVariable String trackingNumber) {
        return consultarHistorial.ejecutar(trackingNumber).stream()
                .map(EventoLogisticoResponse::from)
                .toList();
    }
}
