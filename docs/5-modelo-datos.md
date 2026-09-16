# 5. Modelo de datos

Cuatro tablas, una sola base de datos, y **ninguna clave foránea entre módulos**.

El diagrama está en [`modelo-datos.puml`](modelo-datos.puml).

## `shipments` — módulo shipments

El agregado principal. Una fila por envío.

| Columna | Tipo | Notas |
|---|---|---|
| `id` | bigserial | PK |
| `tracking_number` | varchar | **único**, identificador de negocio |
| `sender_full_name`, `sender_document_id`, `sender_phone`, `sender_address`, `sender_city` | varchar | remitente |
| `recipient_full_name`, `recipient_document_id`, `recipient_phone`, `recipient_address`, `recipient_city` | varchar | destinatario |
| `description` | varchar | descripción del paquete |
| `status` | varchar | estado actual del envío |
| `registered_at` | timestamptz | |
| `last_movement_point`, `last_movement_at` | varchar, timestamptz | nulos hasta el primer evento |

Remitente y destinatario no son tablas aparte: son el objeto embebido `Party`, que se aplana en
columnas con prefijo. No existe una entidad "cliente" porque el sprint no la requiere.

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
| `registered_at` | timestamptz | |

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

Hoy lo genera Hibernate con `spring.jpa.hibernate.ddl-auto=update`, lo que es cómodo para el
sprint pero deja el esquema sin historial ni control de versiones.

**Recomendación para el siguiente sprint:** migrar a Flyway con scripts versionados, para que
el esquema sea reproducible y auditable.
