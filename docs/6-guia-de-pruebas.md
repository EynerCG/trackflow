# 6. Guía de pruebas

Documento para quien escriba las pruebas del Sprint 1 (tareas 77, 79 y 81 del tablero).
`src/test` está intacto, tal como lo generó Spring Initializr.

## Lo primero: el sistema es asíncrono

**Las operaciones de escritura responden `202 Accepted`, no `201`.** La petición HTTP solo
publica en RabbitMQ; el registro ocurre milisegundos después, cuando el consumidor procesa el
mensaje.

Consecuencia directa para las pruebas: una prueba que haga POST y consulte en la línea
siguiente **fallará de forma intermitente**. No es un defecto del producto, es el diseño
(ver [ADR-002](adr/ADR-002-ingesta-por-eventos.md)).

Para las pruebas de aceptación hay que esperar a que el efecto ocurra, con reintentos y un
tiempo límite, en lugar de comprobar de inmediato. Por ejemplo, consultar el estado cada 200 ms
hasta que cambie, con un máximo de unos segundos.

## Lo segundo: el documento cambió de forma

`documento` ya no existe. En su lugar van **dos campos**: `tipoDocumento` (`CC`, `CE`, `TI`,
`PP` o `NIT`) y `numeroDocumento`. Una petición con el formato anterior responde `400`.

El número se valida contra el tipo, así que `{"tipoDocumento": "CC", "numeroDocumento": "1001"}`
también responde `400`: una cédula son entre 6 y 10 dígitos. Los formatos están en
[4. Ejecución](4-ejecucion.md) y el porqué en
[ADR-008](adr/ADR-008-tipo-y-numero-de-documento.md).

## Lo tercero: la ciudad ya no es texto

`ciudad` desaparece y en su lugar va `ciudadId`, un identificador del catálogo. Se obtiene con
`GET /api/ciudades?q=bogo`, que es lo que alimenta el autocompletado. Un id que no está en el
catálogo responde `400`.

Aplica a las dos: ciudad de origen y ciudad de destino
([ADR-010](adr/ADR-010-catalogo-de-ciudades.md)).

## Lo cuarto: el evento puede decir cuándo ocurrió

`POST /api/shipments/{tracking}/events` acepta `ocurridoEn`, opcional. Sin él se asume que el
movimiento acaba de ocurrir.

Para las pruebas importa una regla: **un movimiento anterior al último aplicado se guarda en el
historial pero no cambia el estado**. Es lo que impide que un reporte rezagado devuelva a "en
tránsito" un envío entregado ([ADR-009](adr/ADR-009-fecha-de-ocurrencia-del-movimiento.md)).

## Qué conviene probar, por nivel

### Pruebas unitarias (sin base de datos ni broker)

Son las de las tareas 77, 79 y 81. Se prueban los casos de uso con dobles de los puertos.

| Qué | Dónde | Por qué importa |
|---|---|---|
| Un `eventId` repetido no duplica el movimiento | `RegistrarEventoLogistico` | Es la idempotencia que protege de las reentregas del broker; hoy no está verificada |
| Un `trackingNumber` repetido no registra dos veces el envío | `RegistrarEnvio` | Misma razón, en el flujo de altas |
| Cada tipo de evento lleva al estado correcto | `EventType` | Es la regla de negocio central de HU-02 |
| Se rechazan los formatos inválidos | `TrackingNumber` | El value object es quien garantiza el formato |
| Cada tipo de documento acepta su formato y rechaza los demás | `TipoDocumento` | Incluye el dígito de verificación del NIT |
| Un documento inválido impide construir la persona | `Party` | La regla está en el dominio para que proteja también la entrada por cola |
| Un movimiento anterior al último no cambia el estado | `Shipment.aplicarMovimiento`, `ShipmentTrackingView.aplicarMovimiento` | Es lo que impide que el estado retroceda; hay que probarlo en los dos |
| Un movimiento con fecha futura se rechaza | `LogisticsEvent.registrar` | |
| Una ciudad fuera del catálogo se rechaza al admitir | `AdmitirEnvio` | Falla antes de encolar, no en el consumidor |
| Se lanza la excepción cuando el envío no existe | `ConsultarEstadoEnvio`, `AdmitirEventoLogistico` | Sostiene el 404 de HU-02 y HU-03 |
| El estado inicial es `REGISTERED` | `Shipment.registrar` | Primer criterio de HU-01 |
| Se publica el evento de integración al registrar | `RegistrarEnvio`, `RegistrarEventoLogistico` | Si no se publica, las proyecciones no se enteran |

### Pruebas de aceptación (con infraestructura)

Cubren los nueve criterios de las tres historias. Necesitan base de datos y broker: se levantan
con `docker compose up -d` (ver [4. Ejecución](4-ejecucion.md)).

Aquí es donde aplica la advertencia sobre la asincronía.

## Autenticación en las pruebas

En local **no hace falta token**: si no se define `TRACKFLOW_OPERADOR_CLAVE`, la protección se
desactiva y los endpoints de escritura quedan abiertos. Las pruebas no necesitan credenciales.

Si se quiere probar también el camino autenticado, basta definir esa variable y obtener el token
en `POST /api/auth/login` (ver [4. Ejecución](4-ejecucion.md)).

## Datos semilla

Se activan con `--trackflow.seed.enabled=true` y entran por las mismas colas que cualquier otro
productor. Permiten probar HU-02 y HU-03 sin ejecutar HU-01 antes, como pide el plan de calidad.

| Número | Estado | Sirve para |
|---|---|---|
| `TF000000000001` | Registrado, sin movimientos | "Envío sin movimientos" de HU-03 |
| `TF000000000002` | En tránsito, con dos eventos | "Consulta con último movimiento" de HU-03 |
| `TF000000000003` | Entregado, con tres eventos | "Registro de la entrega" de HU-02 |
| `TF000000000000` | No existe | Escenarios de número inexistente |

La carga es idempotente: si ya están, no se duplican.

## Qué NO probar contra el servicio desplegado

El entorno de Render escribe en la base de datos real. Las pruebas deben ejecutarse en local
contra los contenedores; el despliegue se usa para revisión manual y demostración.

## Cobertura

Las tareas de calidad incluyen JaCoCo y SonarCloud (tarea 84). La cobertura se mide sobre las
pruebas unitarias, así que la verificación manual —por completa que sea— no aporta a ese
indicador.

## Estado actual de la verificación

Los nueve criterios de aceptación **se verificaron manualmente** el 2026-09-16 contra Supabase y
RabbitMQ reales, incluidos los tres escenarios de error. Esa verificación encontró un
incumplimiento real: la respuesta de validación no indicaba qué campo faltaba, lo que
contradecía el segundo criterio de HU-01; se corrigió.

Pero **no queda ninguna prueba automatizada**: nada impide que una regresión pase inadvertida.
Ese es el hueco que cubren estas tareas.
