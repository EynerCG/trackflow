package com.trackflow.modules.shipments.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarEnvioRequest(
        @NotNull(message = "los datos del remitente son obligatorios") @Valid PersonaRequest remitente,
        @NotNull(message = "los datos del destinatario son obligatorios") @Valid PersonaRequest destinatario,
        @NotBlank(message = "la descripción del paquete es obligatoria") String descripcion) {
}
