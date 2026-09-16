package com.trackflow.modules.shipments.api.dto;

import com.trackflow.modules.shipments.domain.Party;
import jakarta.validation.constraints.NotBlank;

public record PersonaRequest(
        @NotBlank(message = "el nombre completo es obligatorio") String nombreCompleto,
        @NotBlank(message = "el documento de identidad es obligatorio") String documento,
        @NotBlank(message = "el teléfono es obligatorio") String telefono,
        @NotBlank(message = "la dirección es obligatoria") String direccion,
        @NotBlank(message = "la ciudad es obligatoria") String ciudad) {

    public Party toDomain() {
        return new Party(nombreCompleto, documento, telefono, direccion, ciudad);
    }
}
