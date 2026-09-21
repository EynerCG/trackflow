# 2. Arquitectura

## Estilo arquitectónico

**Monolito modular**, con cuatro decisiones que lo definen:

| Nivel | Decisión |
|---|---|
| Despliegue | Un solo artefacto y una sola base de datos |
| Organización | Módulos por dominio de negocio, no por capa técnica |
| Interior de cada módulo | Clean Architecture (puertos y adaptadores) |
| Entrada de información | Event-driven: la escritura entra por RabbitMQ |
| Integración entre módulos | Eventos en proceso, sin broker |
| Consulta | CQRS parcial: `reports` mantiene su modelo de lectura |

## Estructura de paquetes

```
com.trackflow
├── TrackflowApplication.java
├── bootstrap/                        ← composición: seeder y manejadores transversales
├── modules/
│   ├── shipments/                    ← HU-01
│   │   ├── api/          + dto/      ← adaptador de entrada REST
│   │   ├── application/              ← casos de uso y PUERTOS
│   │   ├── domain/                   ← agregado, value objects, reglas
│   │   └── infrastructure/ + messaging/  ← adaptadores: JPA y RabbitMQ
│   ├── logistics/                    ← HU-02
│   │   └── (misma estructura)
│   └── reports/                      ← HU-03
│       └── (misma estructura)
└── shared/
    ├── events/                       ← contrato de integración entre módulos
    └── geografia/                    ← catálogo de ciudades (dato de referencia)
```

### La regla de dependencia

Dentro de cada módulo las dependencias apuntan hacia el dominio:

```
api ──→ application ──→ domain
             ▲
      infrastructure
   (implementa los puertos)
```

Las interfaces (puertos) viven en `application` y sus implementaciones en `infrastructure`.
Por eso `infrastructure` depende de `application` y nunca al revés: cambiar Postgres por otra
base, o RabbitMQ por otro broker, no toca los casos de uso.

**Puertos declarados:**

| Puerto | Módulo | Implementación |
|---|---|---|
| `ShipmentRepository` | shipments | `JpaShipmentRepository` |
| `TrackingNumberGenerator` | shipments | `UuidTrackingNumberGenerator` |
| `EnvioSolicitadoPublisher` | shipments | `RabbitMQEnvioSolicitadoPublisher` |
| `LogisticsEventRepository` | logistics | `JpaLogisticsEventRepository` |
| `TrackedShipmentRepository` | logistics | `JpaTrackedShipmentRepository` |
| `EventoLogisticoPublisher` | logistics | `RabbitMQEventoLogisticoPublisher` |
| `ShipmentTrackingViewRepository` | reports | `JpaShipmentTrackingViewRepository` |
| `EventPublisher` | shared | `SpringEventPublisher` |
| `CatalogoDeCiudades` | shared | `CatalogoDeCiudadesJpa` |

## Los tres módulos

**`shipments`** es dueño del envío: lo registra, le asigna su número de seguimiento y mantiene
su estado. Contiene el agregado `Shipment`, el value object `TrackingNumber` (que encapsula el
formato) y `Party` como objeto embebido para remitente y destinatario.

**`logistics`** registra lo que ocurre en cada punto de la cadena. Mantiene el historial en
`LogisticsEvent` y una proyección propia (`TrackedShipment`) con los envíos que conoce, para
poder validar que un envío existe sin consultar las tablas de `shipments`.

**`reports`** responde las consultas. Mantiene `ShipmentTrackingView`, un modelo de lectura
que se construye escuchando los eventos de los otros dos módulos.

**Ningún módulo importa clases de otro.** Solo dependen de `shared`: de `events` para el contrato
de integración y de `geografia` para el catálogo de ciudades, que es dato de referencia del que
ningún módulo de negocio es dueño ([ADR-010](adr/ADR-010-catalogo-de-ciudades.md)).

## Los dos planos de eventos

Es la distinción clave del diseño y conviene no mezclarlas:

### Plano 1 — Ingesta (RabbitMQ)

Cómo entra la información al sistema. Los controladores REST **no escriben en la base de
datos**: validan, publican en el broker y responden `202 Accepted`.

| Flujo | Exchange (topic) | Routing key | Cola | Cola de descarte |
|---|---|---|---|---|
| Alta de envíos | `shipments.topic` | `shipment.requested` | `shipments.requests.queue` | `shipments.requests.dlq` |
| Eventos logísticos | `logistics.topic` | `logistic.event.registered` | `logistics.events.queue` | `logistics.events.dlq` |

Quien escribe son los `@RabbitListener`. Un escáner de un centro de distribución publica en la
misma cola y entra por el mismo camino que el operador con Swagger.

**Idempotencia:** RabbitMQ entrega *at-least-once*, así que una reentrega se descarta en lugar
de duplicar — por `tracking_number` en los envíos y por `event_id` en los eventos logísticos.

**Por qué 202 y no 201:** el registro ocurre al consumir el mensaje, no en la petición. El
número de seguimiento se genera antes de publicar, de modo que el remitente lo recibe en el
momento, como exige HU-01.

### Plano 2 — Integración entre módulos (en proceso)

Cuando un módulo necesita que otros reaccionen, publica un evento de integración de
`shared/events` mediante `EventPublisher`. La entrega es con `@TransactionalEventListener`
tras el commit, y cada consumidor abre su propia transacción.

| Evento | Publica | Consumen |
|---|---|---|
| `EnvioCreadoEvent` | shipments | logistics (registra el envío como conocido), reports (crea la vista) |
| `EventoLogisticoRegistradoEvent` | logistics | shipments (mueve el estado), reports (actualiza el último movimiento) |

Aquí **no** se usa el broker a propósito: dentro del mismo proceso los eventos de Spring bastan
y conservan las garantías transaccionales. Si un módulo se extrajera a un microservicio, esos
dos records serían el contrato del mensaje que viajaría por la red.

## Catálogo de estados

| Evento logístico | Estado resultante del envío |
|---|---|
| `RECEIVED_AT_CENTER` | `AT_DISTRIBUTION_CENTER` |
| `DISPATCHED` | `IN_TRANSIT` |
| `ARRIVED_AT_DESTINATION_CENTER` | `AT_DESTINATION_CENTER` |
| `OUT_FOR_DELIVERY` | `OUT_FOR_DELIVERY` |
| `DELIVERED` | `DELIVERED` |

El estado inicial de todo envío es `REGISTERED`. Son seis estados para seis etapas: el
centro de destino tiene el suyo aunque físicamente también sea "estar en un centro",
porque mientras compartió `AT_DISTRIBUTION_CENTER` con el de origen, llegar a la ciudad
de destino parecía un retroceso para cualquiera que dibujara el progreso desde el estado.

## Diagramas

- [`arquitectura.puml`](arquitectura.puml) — componentes, interfaces y flujo de eventos
- [`modelo-datos.puml`](modelo-datos.puml) — tablas por módulo

Se visualizan con la extensión PlantUML de VS Code (Alt+D).

## Seguridad

La escritura exige un token JWT de operador; la consulta de estado es pública, porque el número
de seguimiento es la credencial del cliente. Sin credenciales configuradas la protección se
desactiva, para no entorpecer el entorno local ni las pruebas. Ver
[ADR-007](adr/ADR-007-autenticacion-con-jwt.md).

## Operación

- **Colas de descarte:** cada mensaje rechazado queda registrado en el log con nivel `ERROR`.
- **Reconstrucción:** `POST /api/admin/reconstruir-proyecciones` rehace las proyecciones
  republicando los eventos desde la fuente de verdad, sin que ningún módulo lea tablas ajenas.
- **Esquema:** lo gobierna Flyway; Hibernate solo valida. Ver
  [ADR-006](adr/ADR-006-flyway-para-el-esquema.md).

## Consecuencias asumidas

**Consistencia eventual.** Entre el `202` y el registro efectivo pasan milisegundos. Una prueba
automatizada que consulte inmediatamente después de escribir puede fallar de forma intermitente.

**Datos duplicados.** El estado del envío vive en `shipments` y en la proyección de `reports`.
Es el precio de la autonomía entre módulos.

**Dependencia del broker.** Si RabbitMQ no está disponible, no se registra nada. A cambio, los
mensajes sobreviven a una caída de la aplicación, cosa que una escritura síncrona no hace.

Las decisiones y sus alternativas están registradas en los [ADR](adr/).
