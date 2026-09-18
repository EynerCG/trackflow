package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.TipoDocumento;

/**
 * Datos de una persona tal como llegan en la solicitud, con la ciudad todavía sin
 * resolver contra el catálogo. Quien la resuelve es {@link AdmitirEnvio}, para que
 * la regla no dependa del adaptador por el que entró la solicitud.
 */
public record DatosPersona(
        String nombreCompleto,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String telefono,
        String direccion,
        Long ciudadId) {
}
