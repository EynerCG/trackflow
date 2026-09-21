package com.trackflow.modules.logistics.api.dto;

import com.trackflow.modules.logistics.application.ConsultarAccionesDisponibles.Accion;
import com.trackflow.modules.logistics.application.ConsultarAccionesDisponibles.Acciones;
import com.trackflow.modules.logistics.application.EstadoDelEnvio;
import com.trackflow.shared.geografia.Ciudad;
import java.time.Instant;
import java.util.List;

/**
 * Lo que el operador puede hacer con un envío ahora mismo, ya resuelto: qué
 * movimientos caben, entre qué centros escoger para cada uno y qué ruta representa.
 * La interfaz lo pinta tal cual, sin decidir nada por su cuenta.
 */
public record AccionesDisponiblesResponse(
        String trackingNumber,
        String estadoActual,
        String ultimoMovimiento,
        Instant ultimoMovimientoEn,
        UbicacionResponse ubicacionActual,
        CiudadResponse origen,
        CiudadResponse destino,
        List<AccionResponse> acciones) {

    public record CiudadResponse(Long id, String nombre) {

        static CiudadResponse from(Ciudad ciudad) {
            return ciudad == null ? null : new CiudadResponse(ciudad.id(), ciudad.etiqueta());
        }
    }

    /** Dónde está el paquete. El centro es null si todavía no ha pasado por ninguno. */
    public record UbicacionResponse(Long centroId, String centroNombre, Long ciudadId, String ciudadNombre) {
    }

    /**
     * @param ciudadEsperada por qué se ofrecen esos centros y no otros (ORIGEN,
     *        DESTINO, ACTUAL, HUB_INTERMEDIO), para que la interfaz pueda explicarlo
     * @param ruta trayecto del movimiento; solo el despacho tiene, el resto es null
     */
    public record AccionResponse(
            String tipo,
            String etiqueta,
            String estadoResultante,
            String ciudadEsperada,
            CiudadResponse ciudadDeLosCentros,
            List<CentroResponse> centros,
            String ruta) {
    }

    public static AccionesDisponiblesResponse from(Acciones acciones) {
        EstadoDelEnvio estado = acciones.estado();

        return new AccionesDisponiblesResponse(
                estado.trackingNumber(),
                estado.estado(),
                estado.ultimoTipo() == null ? null : estado.ultimoTipo().name(),
                estado.ultimoMovimientoEn(),
                new UbicacionResponse(
                        estado.centroActualId(),
                        estado.centroActualNombre(),
                        estado.ciudadActual().id(),
                        estado.ciudadActual().etiqueta()),
                CiudadResponse.from(estado.origen()),
                CiudadResponse.from(estado.destino()),
                acciones.acciones().stream().map(AccionesDisponiblesResponse::aAccion).toList());
    }

    private static AccionResponse aAccion(Accion accion) {
        return new AccionResponse(
                accion.tipo().name(),
                accion.tipo().etiqueta(),
                accion.tipo().resultingStatus(),
                accion.ciudadEsperada().name(),
                CiudadResponse.from(accion.ciudadDeLosCentros()),
                accion.centros().stream()
                        .map(disponible -> CentroResponse.from(disponible.centro(), disponible.ciudad()))
                        .toList(),
                accion.ruta());
    }
}
