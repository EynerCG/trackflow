# 1. Enunciado

## Contexto de negocio

Las empresas de logística gestionan miles de envíos diarios entre centros de distribución,
vehículos de transporte y destinos finales. Para clientes y operadores, la visibilidad del
estado de cada envío es fundamental para garantizar confianza en el servicio.

Muchas organizaciones aún dependen de sistemas fragmentados o actualizaciones manuales, que
dificultan conocer con precisión dónde se encuentra un paquete y cuál es su estado actual
dentro de la cadena logística.

## Problema a resolver

Desarrollar un sistema que permita registrar y consultar el ciclo de vida de un envío dentro
de una red logística, contemplando:

- Registro de envíos y datos del remitente y destinatario
- Generación de identificadores únicos de seguimiento
- Registro de eventos logísticos en cada punto de la cadena de transporte
- Actualización del estado del paquete según su etapa logística
- Consulta del historial completo de movimientos de un envío
- Reportes operativos sobre tiempos de tránsito, retrasos y volumen de envíos

## Alcance del Sprint 1

De lo anterior, este sprint cubre las tres primeras capacidades más la consulta de estado.
Los reportes operativos quedan fuera.

### HU-01 — Registrar un envío con remitente y destinatario

> Como **operador de logística** quiero registrar un envío con los datos de su remitente y su
> destinatario, para que el paquete entre a la red con un número de seguimiento propio.

**Criterios de aceptación**

1. *Registro con datos completos:* dado que tengo los datos del remitente y del destinatario,
   cuando registro el envío, entonces queda registrado con su estado inicial y recibo un
   número de seguimiento para entregarle al remitente.
2. *Registro con datos obligatorios incompletos:* dado que faltan datos obligatorios del
   destinatario, cuando intento registrar el envío, entonces no queda registrado y se me
   indica cuáles datos faltan.
3. *Números de seguimiento distintos:* dado que registro dos envíos diferentes el mismo día,
   cuando consulto sus números, entonces cada uno tiene un número distinto del otro.

**Fuera de alcance:** cotización y cobro, modificación o anulación de un envío registrado,
y los eventos posteriores del envío.

### HU-02 — Registrar un evento logístico de un envío

> Como **operador de un punto de la cadena logística** quiero registrar lo que ocurre con un
> envío a su paso por mi punto, para que su estado refleje dónde está y en qué etapa va.

**Criterios de aceptación**

1. *Evento sobre un envío existente:* el evento queda en el historial con su fecha y su punto,
   y el estado actual del envío pasa a ser el que corresponde a ese evento.
2. *Registro de la entrega:* el envío queda con estado entregado y la entrega aparece como su
   último movimiento.
3. *Evento sobre un número inexistente:* el evento no se registra y se informa que el envío
   no existe.

**Fuera de alcance:** notificaciones automáticas al cliente, corrección o eliminación de un
evento registrado, y captura automática de la ubicación del vehículo.

### HU-03 — Consultar el estado actual de un envío

> Como **cliente** quiero consultar el estado actual de mi envío con su número de seguimiento,
> para saber dónde está sin tener que llamar a servicio al cliente.

**Criterios de aceptación**

1. *Consulta con un número válido:* veo el estado actual del envío y el punto y la fecha de su
   último movimiento.
2. *Envío sin movimientos:* veo su estado inicial y que aún no registra movimientos.
3. *Número inexistente:* se informa que no se encontró ningún envío con ese número y no se
   muestra información de ningún otro envío.

**Fuera de alcance:** historial completo de movimientos (HU-04), suscripción a cambios de
estado y fecha estimada de entrega.

## Supuestos tomados

Las historias dejaron preguntas abiertas que hubo que resolver para poder implementar. Estas
decisiones **están tomadas en el código pero no han sido validadas con el dueño de producto**:

| Pregunta abierta | Supuesto aplicado |
|---|---|
| ¿Qué datos del remitente y destinatario son obligatorios? | Todos: nombre, tipo y número de documento, teléfono, dirección y ciudad |
| ¿De dónde sale la ciudad? | De un catálogo; se envía su id, no el nombre ([ADR-010](adr/ADR-010-catalogo-de-ciudades.md)) |
| ¿Quién fija la fecha de un movimiento? | La reporta el punto de la cadena; si no la envía, se asume el momento de recepción ([ADR-009](adr/ADR-009-fecha-de-ocurrencia-del-movimiento.md)) |
| ¿Qué documentos se admiten? | `CC`, `CE`, `TI`, `PP` y `NIT`, cada uno con su formato validado ([ADR-008](adr/ADR-008-tipo-y-numero-de-documento.md)) |
| ¿Qué formato tiene el número de seguimiento? | `TF` + 12 caracteres alfanuméricos en mayúscula |
| ¿Cuál es el estado inicial de un envío? | `REGISTERED` |
| ¿Un registro cubre varios paquetes? | Un envío por paquete |
| ¿Qué transiciones de estado son válidas? | Solo las del recorrido: de un centro se despacha, en tránsito se llega al destino o a un hub, y del reparto se entrega ([docs/7](7-flujo-del-envio.md)) |
| ¿Qué pasa si llega un evento sobre un envío entregado? | Se rechaza con `422`: entregado es terminal |
| ¿Dónde puede ocurrir cada movimiento? | En un centro del catálogo, en la ciudad que el recorrido exige: origen, destino, o donde está el paquete |
| ¿Quién puede registrar envíos y eventos? | Sin autenticación en este sprint |
