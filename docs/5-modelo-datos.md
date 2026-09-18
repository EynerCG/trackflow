# 5. Modelo de datos

Cuatro tablas, una sola base de datos, y **ninguna clave foránea entre módulos**.

El diagrama está en [`modelo-datos.puml`](modelo-datos.puml).

## `shipments` — módulo shipments

El agregado principal. Una fila por envío.

| Columna | Tipo | Notas |
|---|---|---|
| `id` | bigserial | PK |
| `tracking_number` | varchar | **único**, identificador de negocio |
| `sender_full_name`, `sender_document_type`, `sender_document_number`, `sender_phone`, `sender_address`, `sender_city` | varchar | remitente |
| `recipient_full_name`, `recipient_document_type`, `recipient_document_number`, `recipient_phone`, `recipient_address`, `recipient_city` | varchar | destinatario |
| `description` | varchar | descripción del paquete |
| `status` | varchar | estado actual del envío |
| `registered_at` | timestamptz | |
| `last_movement_point`, `last_movement_at` | varchar, timestamptz | nulos hasta el primer evento |

Remitente y destinatario no son tablas aparte: son el objeto embebido `Party`, que se aplana en
columnas con prefijo. No existe una entidad "cliente" porque el sprint no la requiere.

El documento son **dos columnas**, no una. `*_document_type` guarda el tipo (`CC`, `CE`, `TI`,
`PP`, `NIT`) con un `CHECK` que lo restringe al catálogo, y `*_document_number` el número. El
tipo es el que decide cómo se valida el número, así que juntarlos en un solo texto impedía
validar y también contar cuántos envíos los despacha una empresa (`NIT`) frente a una persona
natural. Ver [ADR-008](adr/ADR-008-tipo-y-numero-de-documento.md).

## `logistics_events` — módulo logistics

El historial de movimientos. Una fila por evento reportado.

| Columna | Tipo | Notas |
|---|---|---|
| `id` | bigserial | PK |
| `event_id` | varchar | **único**, descarta reentregas del broker |
| `tracking_number` | varchar | sin FK |
| `type` | varchar | tipo de evento logístico |
| `point` | varchar | punto de la cadena donde ocurrió |
| `notes` | varchar | opcional |
| `occurred_at` | timestamptz | **cuándo ocurrió** el movimiento; ordena el historial |
| `registered_at` | timestamptz | cuándo se recibió el reporte |

Las dos fechas no son lo mismo y confundirlas hacía retroceder el estado del envío. Ver
[ADR-009](adr/ADR-009-fecha-de-ocurrencia-del-movimiento.md).

## `logistics_tracked_shipments` — módulo logistics

Los envíos que este módulo conoce. Se llena al consumir `EnvioCreadoEvent`.

| Columna | Tipo |
|---|---|
| `tracking_number` | varchar, PK |
| `registered_at` | timestamptz |

Existe para que `logistics` pueda responder "ese envío no existe" sin leer la tabla de otro
módulo. Es deliberadamente mínima: solo lo necesario para esa validación.

## `reports_shipment_tracking` — módulo reports

El modelo de lectura de HU-03. Lo construyen los dos eventos de integración.

| Columna | Tipo |
|---|---|
| `tracking_number` | varchar, PK |
| `status` | varchar |
| `recipient_name` | varchar |
| `destination_city` | varchar |
| `registered_at` | timestamptz |
| `last_movement_point`, `last_movement_at` | varchar, timestamptz |

Contiene solo lo que el cliente puede ver: no expone remitente, teléfonos ni direcciones, que
son datos de operación.

## Dos decisiones que explican el modelo

### No hay claves foráneas entre módulos

Las tablas se correlacionan por `tracking_number` como identificador de negocio. Una FK ataría
los módulos a nivel de base de datos y anularía la independencia lograda en el código: si
mañana `logistics` se extrae a un microservicio con su propia base, el modelo no cambia.

El precio es que la integridad referencial no la garantiza el motor, sino el flujo de eventos y
la validación de los casos de uso.

### Hay duplicación, y es intencional

El estado del envío vive en `shipments` y en `reports_shipment_tracking`; el número de
seguimiento aparece en las cuatro tablas. Es la contrapartida de la modularidad y del CQRS: se
gana autonomía y consultas directas, se paga con datos repetidos que se sincronizan por eventos.

## Creación del esquema

El esquema lo gobierna **Flyway**, con migraciones versionadas en
`src/main/resources/db/migration`. Hibernate está en `ddl-auto=validate`: verifica al arrancar
que las entidades coincidan con las tablas, pero no modifica nada.

Esto significa que **cada cambio en una entidad necesita su migración**. Si se olvida, la
aplicación no arranca y el log indica qué no coincide.

Las bases creadas antes de adoptar Flyway se marcan como línea base
(`spring.flyway.baseline-on-migrate=true`) en lugar de intentar recrear sus tablas.

Ver [ADR-006](adr/ADR-006-flyway-para-el-esquema.md).
