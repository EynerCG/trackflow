# ADR-005 — Modelo de lectura propio para la consulta de estado

- **Estado:** Aceptada
- **Fecha:** 2026-09-15
- **Responsable:** Eyner Gómez Quintero

## Contexto

HU-03 pide que un cliente consulte el estado actual de su envío y vea el punto y la fecha de su
último movimiento. Esos datos nacen en dos módulos distintos: el envío y su estado pertenecen a
`shipments`, mientras que los movimientos los registra `logistics`.

La consulta la hace el cliente final, es la operación más frecuente del sistema (el caso de
negocio la señala como la pregunta más común a servicio al cliente) y debe ser rápida.

## Alternativas consideradas

**A. Consultar el agregado directamente desde `shipments`.** Lo más simple: HU-03 vive en
`shipments` y lee la tabla de envíos. Pero expone al cliente el mismo modelo que usa la
operación —con remitente, documentos, teléfonos y direcciones— y obliga a decidir qué ocultar
en cada consulta. Además deja el módulo `reports` sin contenido en este sprint.

**B. Consultar y unir datos de los dos módulos.** Una consulta que cruce envíos y eventos. Es
la opción más natural en un modelo relacional, pero exige un JOIN entre tablas de módulos
distintos, que es justo lo que la modularidad prohíbe (ADR-001).

**C. Modelo de lectura propio, alimentado por eventos.** Un módulo `reports` con su propia
tabla, construida escuchando los eventos de los otros dos.

## Decisión

Se adopta la alternativa **C**.

`reports` mantiene `ShipmentTrackingView`, una tabla que se actualiza al recibir
`EnvioCreadoEvent` y `EventoLogisticoRegistradoEvent`. La consulta de HU-03 lee solo esa tabla.

## Consecuencias

**Positivas**

- La consulta es una lectura por clave primaria, sin uniones: escala bien para la operación más
  frecuente del sistema.
- El modelo contiene únicamente lo que el cliente puede ver. La separación entre datos de
  operación y datos de cliente queda estructural, no a criterio de cada consulta.
- Cada módulo mantiene la propiedad de sus tablas: `reports` no lee las de nadie.
- Permite evolucionar la vista de consulta sin tocar el modelo de escritura.

**Negativas**

- El estado del envío queda duplicado: en `shipments` (agregado) y en `reports` (vista).
- Consistencia eventual: la proyección se actualiza tras el commit del evento, con un desfase de
  milisegundos.
- Si un evento de proyección falla, la vista queda desincronizada de forma permanente y no hay
  mecanismo de reconstrucción. **Es la deuda más relevante de esta decisión**: haría falta poder
  reconstruir la proyección desde el historial de eventos.

**Nota sobre el nombre**

El módulo se llama `reports` pero hoy solo contiene la consulta de estado. El caso de negocio
contempla reportes operativos de tiempos de tránsito, retrasos y volumen, que serían su
contenido natural en sprints siguientes. Si esos reportes no llegan, convendría renombrarlo a
`tracking`, que describe mejor lo que hace.
