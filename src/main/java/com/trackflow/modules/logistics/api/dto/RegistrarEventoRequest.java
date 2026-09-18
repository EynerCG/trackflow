package com.trackflow.modules.logistics.api.dto;

import com.trackflow.modules.logistics.domain.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;

public record RegistrarEventoRequest(
        @NotNull(message = "el tipo de evento es obligatorio") EventType tipo,
        @NotBlank(message = "el punto de la cadena logística es obligatorio") String punto,
        String observaciones,

        /**
         * Cuándo ocurrió el movimiento. Opcional: si no se envía se asume que acaba de
         * ocurrir. Se reporta cuando el registro se sincroniza tarde, que es lo normal
         * si el lector de la bodega estuvo sin señal.
         */
        @PastOrPresent(message = "el movimiento no puede haber ocurrido en el futuro")
        Instant ocurridoEn) {
}
