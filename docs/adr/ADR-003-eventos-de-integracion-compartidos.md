# ADR-003 — Eventos de integración en `shared/events`

- **Estado:** Aceptada
- **Fecha:** 2026-09-15
- **Responsable:** Eyner Gómez Quintero

## Contexto

Los módulos se comunican publicando eventos (ADR-001). Faltaba decidir **dónde vive la clase**
de cada evento, que es lo que determina quién depende de quién.

La primera versión los puso a todos en `shared/events`. Después se movieron a
`domain/events/` de cada módulo, siguiendo la idea de que un evento de dominio pertenece al
dominio que lo emite.

Al revisar las dependencias apareció el problema: `shipments` importaba
`logistics.domain.events.EventoLogisticoRegistradoEvent` y `logistics` importaba
`shipments.domain.events.EnvioCreadoEvent`. Un **ciclo de dependencias** entre los dos módulos,
porque el suscriptor necesita la clase del publicador para escucharla.

Un ciclo así invalida la premisa del monolito modular: los módulos ya no son independientes y
ninguno podría extraerse sin arrastrar al otro.

## Alternativas consideradas

**A. Eventos en el dominio de cada módulo.** Coherente con DDD: el evento pertenece a quien lo
emite. Pero genera el ciclo descrito, porque el consumidor importa la clase del productor.

**B. Eventos de integración en `shared/events`.** Se distingue entre el evento *de dominio*
(interno de un módulo) y el evento *de integración* (el contrato que cruza la frontera). Solo
el segundo se comparte. Todos los módulos dependen del contrato común y ninguno de otro módulo.

**C. Traducción en cada consumidor (capa anticorrupción).** Cada módulo define su propia clase
de evento entrante y un adaptador traduce. Elimina el ciclo sin compartir nada, a costa de
duplicar la definición del evento y escribir traductores.

## Decisión

Se adopta la alternativa **B**.

`shared/events` contiene el contrato de integración: `DomainEvent`, `EventPublisher`,
`EnvioCreadoEvent` y `EventoLogisticoRegistradoEvent`. Los módulos dependen de ese paquete y de
ninguno otro. Un evento puramente interno de un módulo sí viviría en su propio `domain`.

## Consecuencias

**Positivas**

- El grafo de dependencias queda acíclico: los tres módulos apuntan a `shared/events` y ninguno
  apunta a otro módulo. Verificado revisando las importaciones de todo el código fuente.
- El contrato entre módulos es explícito y está en un solo lugar, lo que facilita razonar sobre
  qué se puede cambiar sin romper a nadie.
- Esos dos records son exactamente el contrato del mensaje que viajaría por la red si un módulo
  se extrajera a un microservicio.

**Negativas**

- `shared/events` es un punto de acoplamiento común: un cambio en un evento afecta a todos sus
  consumidores, así que evoluciona con cuidado (añadir campos, no renombrarlos).
- Existe el riesgo de que `shared` crezca como cajón de sastre. Solo deben entrar contratos de
  integración, nunca lógica ni entidades.
- Se pierde algo de pureza DDD: el evento no vive junto al agregado que lo produce.

**Alternativa descartada por ahora**

La capa anticorrupción (opción C) es la solución correcta cuando los módulos evolucionan a
ritmos distintos o pertenecen a equipos diferentes. Con tres módulos en un mismo repositorio y
un solo equipo, el costo de duplicar y traducir no se justifica todavía.
