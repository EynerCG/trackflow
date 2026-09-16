package com.trackflow.modules.shipments.api;

import com.trackflow.modules.shipments.api.dto.EnvioRegistradoResponse;
import com.trackflow.modules.shipments.api.dto.RegistrarEnvioRequest;
import com.trackflow.modules.shipments.application.RegistrarEnvio;
import com.trackflow.modules.shipments.domain.Shipment;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final RegistrarEnvio registrarEnvio;

    public ShipmentController(RegistrarEnvio registrarEnvio) {
        this.registrarEnvio = registrarEnvio;
    }

    @PostMapping
    public ResponseEntity<EnvioRegistradoResponse> registrar(@Valid @RequestBody RegistrarEnvioRequest request) {
        Shipment shipment = registrarEnvio.ejecutar(new RegistrarEnvio.Command(
                request.remitente().toDomain(),
                request.destinatario().toDomain(),
                request.descripcion()));

        return ResponseEntity
                .created(URI.create("/api/tracking/" + shipment.getTrackingNumber().value()))
                .body(EnvioRegistradoResponse.from(shipment));
    }
}
