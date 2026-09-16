# ADR-002 — La información entra por eventos; el REST solo publica

- **Estado:** Aceptada
- **Fecha:** 2026-09-15
- **Responsable:** Eyner Gómez Quintero

## Contexto

En una red logística real, los eventos de un envío los reportan escáneres de centros de
distribución, terminales de vehículos y dispositivos de reparto. Son muchos emisores,
intermitentes y con conectividad variable. El caso de negocio habla de miles de envíos diarios.

La primera versión del sprint registraba directamente en la base de datos dentro de la petición
HTTP y publicaba eventos solo para comunicar los módulos entre sí. Es decir, los eventos eran de
salida, no de entrada.

El requisito del curso es explícito: **el recibimiento de la información debe ser por una
arquitectura de eventos**, y así se va a evaluar.

## Alternativas consideradas

**A. REST síncrono con eventos de salida.** El controlador valida, escribe y después publica un
evento para notificar. Es lo más simple y devuelve `201` con el recurso creado. Pero la escritura
depende de que la base esté disponible en ese instante, y no hay forma de absorber picos: si
llegan mil escaneos a la vez, mil transacciones compiten por la base.

**B. Ingesta por cola, con el REST como productor.** El controlador valida y publica en el
broker; un consumidor registra. El mismo camino sirve para un escáner que publique directamente
en la cola.

**C. Solo cola, sin REST.** Suprimir los endpoints de escritura. Es lo más puro, pero deja al
equipo sin forma de probar ni demostrar las historias sin montar un publicador externo.

## Decisión

Se adopta la alternativa **B**.

Los controladores REST validan la solicitud y la publican en RabbitMQ; ninguna petición HTTP
escribe en la base de datos. Los `@RabbitListener` son los únicos que invocan los casos de uso
de escritura. Se mantienen los endpoints REST porque son el productor más conveniente para
pruebas y sustentación, y porque cualquier otro sistema puede publicar en la misma cola.

## Consecuencias

**Positivas**

- El sistema absorbe picos: las ráfagas se encolan en lugar de saturar la base de datos.
- Si la aplicación cae, los mensajes sobreviven en el broker y se procesan al volver.
- Añadir un nuevo emisor (un escáner, otro sistema) no requiere tocar la aplicación.
- La arquitectura de eventos queda en el camino real de toda la información, no como un
  añadido decorativo.

**Negativas**

- Las respuestas son `202 Accepted`, no `201 Created`: se confirma la admisión, no el registro.
  Para cumplir HU-01 el número de seguimiento se genera **antes** de publicar, de modo que el
  remitente lo reciba en el momento.
- Consistencia eventual: entre la respuesta y el registro efectivo pasan milisegundos. Las
  pruebas automatizadas que consulten inmediatamente después de escribir pueden fallar de forma
  intermitente, y hay que escribirlas teniéndolo en cuenta.
- El broker se vuelve infraestructura crítica: sin él no se registra nada.
- Se necesita idempotencia, porque la entrega es *at-least-once*. Se resuelve con
  `tracking_number` único en los envíos y `event_id` único en los eventos logísticos.
- Los mensajes que no se pueden procesar necesitan destino: se configuraron colas de descarte
  (DLQ) por flujo.

**Seguimiento**

Las colas de descarte tienen un consumidor que registra cada mensaje rechazado en el log con
nivel `ERROR`, con su `eventId` y su número de seguimiento. Queda pendiente poder reprocesarlos
desde la aplicación una vez corregida la causa; hoy hay que republicarlos a mano.
