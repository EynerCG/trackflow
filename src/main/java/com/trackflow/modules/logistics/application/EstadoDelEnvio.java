package com.trackflow.modules.logistics.application;

import com.trackflow.modules.logistics.domain.CiudadEsperada;
import com.trackflow.modules.logistics.domain.EventType;
import com.trackflow.shared.geografia.Ciudad;
import java.time.Instant;
import java.util.Set;

/**
 * Dónde va el envío ahora: estado, ubicación y movimientos admisibles.
 *
 * Se deriva del historial de eventos, no de columnas propias. El historial ya es la
 * fuente de la trazabilidad, así que derivar de él evita el único problema que tiene
 * guardar el estado aparte: que las dos versiones se desincronicen y ya no se sepa
 * cuál manda.
 *
 * @param ultimoTipo último movimiento registrado, o null si el envío aún no se ha movido
 * @param centroActualId último centro por el que pasó, o null si todavía ninguno
 * @param ciudadActual dónde está el paquete; mientras no se haya movido, la de origen
 * @param ultimoMovimientoEn cuándo ocurrió el último movimiento, o null si no hay
 */
public record EstadoDelEnvio(
        String trackingNumber,
        EventType ultimoTipo,
        String estado,
        Instant ultimoMovimientoEn,
        Long centroActualId,
        String centroActualNombre,
        Ciudad ciudadActual,
        Ciudad origen,
        Ciudad destino,
        Set<EventType> siguientesPermitidos) {

    /**
     * Traduce la regla abstracta a la ciudad concreta que exige. Devuelve null para
     * {@link CiudadEsperada#HUB_INTERMEDIO}, que no señala una ciudad sino que excluye
     * la de destino.
     */
    public Ciudad ciudadQueExige(CiudadEsperada esperada) {
        return switch (esperada) {
            case ORIGEN -> origen;
            case DESTINO -> destino;
            case ACTUAL -> ciudadActual;
            case HUB_INTERMEDIO -> null;
        };
    }
}
