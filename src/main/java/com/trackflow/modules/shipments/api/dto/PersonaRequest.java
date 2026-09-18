package com.trackflow.modules.shipments.api.dto;

import com.trackflow.modules.shipments.domain.Party;
import com.trackflow.modules.shipments.domain.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PersonaRequest(
        @NotBlank(message = "el nombre completo es obligatorio") String nombreCompleto,
        @NotNull(message = "el tipo de documento es obligatorio (CC, CE, TI, PP o NIT)")
        TipoDocumento tipoDocumento,
        @NotBlank(message = "el número de documento es obligatorio") String numeroDocumento,
        @NotBlank(message = "el teléfono es obligatorio") String telefono,
        @NotBlank(message = "la dirección es obligatoria") String direccion,
        @NotBlank(message = "la ciudad es obligatoria") String ciudad) {

    public Party toDomain() {
        return new Party(nombreCompleto, tipoDocumento, numeroDocumento, telefono, direccion, ciudad);
    }
}
