package com.trackflow.modules.logistics.api.dto;

import com.trackflow.modules.logistics.domain.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarEventoRequest(
        @NotNull(message = "el tipo de evento es obligatorio") EventType tipo,
        @NotBlank(message = "el punto de la cadena logística es obligatorio") String punto,
        String observaciones) {
}
