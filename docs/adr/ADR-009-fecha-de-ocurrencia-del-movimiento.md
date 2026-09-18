# ADR-009 — El movimiento guarda cuándo ocurrió, no solo cuándo se reportó

- **Estado:** Aceptada
- **Fecha:** 2026-09-18
- **Responsable:** Eyner Gómez Quintero

## Contexto

Un evento logístico tenía una sola fecha, `registered_at`, que el sistema sellaba con su propio
reloj al consumir el mensaje. La API la devolvía en un campo llamado `ocurridoEn`.

Eso era falso. `registered_at` es cuándo el sistema se enteró, no cuándo pasó el paquete por la
bodega, y en logística las dos cosas se separan a diario: el lector de una bodega sin señal
descarga sus registros horas después, el conductor sincroniza al terminar la ruta, un centro
reporta por lotes al cierre del día. El endpoint ni siquiera aceptaba la fecha, así que el punto
de la cadena no tenía forma de decirla.

De ahí salía un segundo problema, este sí un defecto: el estado del envío se aplicaba en orden de
llegada. Un movimiento reportado tarde pisaba a uno posterior y devolvía el envío a "en tránsito"
cuando ya estaba entregado. Lo mismo en el modelo de lectura de `reports`.

## Alternativas consideradas

**A. Dejar una sola fecha.** Nada que hacer. A cambio, el histórico que ve el cliente miente y el
estado puede retroceder.

**B. Dos fechas, y ordenar por la de ocurrencia.** `occurred_at` para cuándo ocurrió el
movimiento y `registered_at` para cuándo se recibió el reporte.

**C. Dos fechas más un número de secuencia por envío.** Resuelve además el empate cuando dos
movimientos ocurren en el mismo instante. Exige que quien reporta lleve la cuenta, que es más de
lo que un lector de bodega puede garantizar.

## Decisión

Se adopta la alternativa **B**.

`POST /api/shipments/{tracking}/events` acepta `ocurridoEn` como campo **opcional**: si no se
envía, se asume que el movimiento acaba de ocurrir, que es el caso normal. El historial se ordena
por `occurred_at` y es esa fecha la que se muestra como la del movimiento.

Un movimiento **no se aplica si es anterior al último aplicado**. La comprobación vive en
`Shipment.aplicarMovimiento` y en `ShipmentTrackingView.aplicarMovimiento`, no en el caso de uso:
es una regla del agregado y debe valer llegue el evento por donde llegue.

Que un movimiento no cambie el estado no lo borra del historial: queda registrado con su fecha, y
se ve en la consulta de movimientos en la posición cronológica que le toca.

## Consecuencias

**Positivas**

- El cliente ve la hora en que su paquete pasó por cada punto, no la hora en que el servidor se
  enteró.
- El estado deja de retroceder por un reporte rezagado o por una reentrega del broker.
- `registered_at - occurred_at` mide cuánto tarda cada punto en reportar, que es un indicador
  operativo real.
- La reconstrucción de proyecciones reaplica el historial en orden de ocurrencia.

**Negativas**

- **Cambia el contrato de la API.** La respuesta del historial ahora trae `ocurridoEn` y
  `registradoEn` en lugar de un solo `registeredAt`.
- Se confía en el reloj de quien reporta. Se rechaza una fecha futura, pero un reloj atrasado
  puede colocar un movimiento antes de lo que ocurrió y hacer que se ignore.
- Dos movimientos con el mismo instante quedan en orden indeterminado. Es la alternativa C, y no
  se tomó.
