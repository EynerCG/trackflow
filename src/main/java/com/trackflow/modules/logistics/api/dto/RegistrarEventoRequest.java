package com.trackflow.modules.logistics.api.dto;

import com.trackflow.modules.logistics.domain.EventType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;

public record RegistrarEventoRequest(
        @NotNull(message = "el tipo de evento es obligatorio") EventType tipo,

        /**
         * Id del centro del catálogo (GET /api/centros). Es el camino preferido: sin
         * él no se pueden aplicar las reglas de coherencia de ciudad.
         */
        Long centroId,

        /**
         * Respaldo de texto libre, deprecado. Se acepta durante la transición al
         * catálogo de centros; si llega centroId, este campo se ignora.
         */
        @Deprecated
        String punto,

        String observaciones,

        /**
         * Cuándo ocurrió el movimiento. Opcional: si no se envía se asume que acaba de
         * ocurrir. Se reporta cuando el registro se sincroniza tarde, que es lo normal
         * si el lector de la bodega estuvo sin señal.
         */
        @PastOrPresent(message = "el movimiento no puede haber ocurrido en el futuro")
        Instant ocurridoEn) {

    @AssertTrue(message = "debe indicar centroId (o, en su defecto, punto)")
    public boolean isCentroOPuntoPresente() {
        return centroId != null || (punto != null && !punto.isBlank());
    }
}
