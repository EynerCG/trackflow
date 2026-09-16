# 4. Ejecución

## Requisitos

- Java 21
- Docker (o RabbitMQ y PostgreSQL instalados — ver [3. Instalación de RabbitMQ](3-instalacion-rabbitmq.md))

Maven no hace falta: el proyecto trae el wrapper (`mvnw`).

## Local, todo en Docker

```bash
docker compose up -d
```

```bash
./mvnw spring-boot:run "-Dspring-boot.run.arguments=--trackflow.seed.enabled=true"
```

Con los valores por defecto la aplicación se conecta a `localhost` tanto para la base de datos
como para el broker, así que no hay que configurar nada más.

Al terminar de arrancar, el log debe mostrar:

```
Datos semilla encolados: TF000000000001 (sin movimientos), TF000000000002 (en tránsito), TF000000000003 (entregado)
```

## Local con base de datos remota

Las credenciales se pasan por variables de entorno. En PowerShell, **en la misma ventana**
donde se vaya a arrancar la aplicación:

```bash
$env:TRACKFLOW_DB_URL="jdbc:postgresql://HOST:5432/postgres?sslmode=require"
```

```bash
$env:TRACKFLOW_DB_USER="usuario"
```

```bash
$env:TRACKFLOW_DB_PASSWORD="contraseña"
```

> Las credenciales nunca se escriben en `application.properties`: ese archivo se versiona y
> terminaría en el repositorio.

## Variables de configuración

| Variable | Por defecto | Para qué |
|---|---|---|
| `PORT` | 8080 | Puerto del servicio (lo inyecta el PaaS) |
| `TRACKFLOW_DB_URL` | `jdbc:postgresql://localhost:5432/trackflow` | Cadena JDBC |
| `TRACKFLOW_DB_USER` | `postgres` | Usuario de la base |
| `TRACKFLOW_DB_PASSWORD` | `postgres` | Contraseña de la base |
| `RABBITMQ_HOST` | `localhost` | Host del broker |
| `RABBITMQ_PORT` | `5672` | Puerto (5671 con TLS) |
| `RABBITMQ_USER` | `guest` | Usuario del broker |
| `RABBITMQ_PASSWORD` | `guest` | Contraseña del broker |
| `RABBITMQ_VHOST` | `/` | Virtual host |
| `RABBITMQ_SSL` | `false` | TLS, obligatorio en brokers gestionados |
| `trackflow.seed.enabled` | `false` | Carga los datos semilla al arrancar |
| `TRACKFLOW_OPERADOR_USUARIO` | `operador` | Usuario que puede escribir |
| `TRACKFLOW_OPERADOR_CLAVE` | *(vacía)* | Su clave. **Vacía desactiva la protección** |
| `TRACKFLOW_ADMIN_USUARIO` | `admin` | Usuario con permisos de mantenimiento |
| `TRACKFLOW_ADMIN_CLAVE` | *(vacía)* | Su clave. Vacía deja `/api/admin/**` inaccesible |
| `TRACKFLOW_JWT_SECRET` | *(generado)* | Secreto de firma, mínimo 32 caracteres |
| `TRACKFLOW_TOKEN_MINUTOS` | `60` | Vigencia del token |

## Autenticación

Hay dos roles y un acceso público:

| Quién | Puede |
|---|---|
| Cualquiera, sin token | Consultar el estado de un envío y su historial |
| `OPERADOR` | Además, registrar envíos y eventos |
| `ADMIN` | Además, reconstruir proyecciones (`/api/admin/**`) |

La consulta es pública a propósito: el número de seguimiento es la credencial del cliente.

En local, si no se define `TRACKFLOW_OPERADOR_CLAVE`, la protección queda desactivada y todo
funciona sin token. El arranque lo advierte en el log. Con la clave definida:

```bash
POST /api/auth/login
{ "usuario": "operador", "clave": "la-clave-configurada" }
```

Devuelve un token que se envía en cada petición de escritura como
`Authorization: Bearer <token>`. En Swagger se registra una vez con el botón **Authorize** y
queda aplicado a todas las llamadas.

## Datos semilla

Se activan solo con `--trackflow.seed.enabled=true`, para que nunca se carguen en un despliegue
real. Se publican en las mismas colas que cualquier otro productor, así que recorren el flujo
completo de eventos.

| Número | Estado | Sirve para |
|---|---|---|
| `TF000000000001` | Registrado, sin movimientos | HU-03 escenario "envío sin movimientos" |
| `TF000000000002` | En tránsito, con historial | HU-03 escenario "consulta con último movimiento" |
| `TF000000000003` | Entregado | HU-02 escenario "registro de la entrega" |
| `TF000000000000` | *No existe* | Escenarios de número inexistente |

La carga es idempotente: si los envíos ya están, no se duplican.

## Probar la aplicación

La forma más cómoda es la documentación interactiva: http://localhost:8080 redirige a
Swagger UI.

### Registrar un envío (HU-01)

`POST /api/shipments`

```json
{
  "remitente": {
    "nombreCompleto": "Ana Remitente",
    "documento": "CC123",
    "telefono": "3001234567",
    "direccion": "Calle 10 #20-30",
    "ciudad": "Medellín"
  },
  "destinatario": {
    "nombreCompleto": "Beto Destinatario",
    "documento": "CC456",
    "telefono": "3007654321",
    "direccion": "Carrera 7 #40-50",
    "ciudad": "Bogotá"
  },
  "descripcion": "Caja de repuestos"
}
```

Responde `202 Accepted` con el número de seguimiento. Si falta un campo obligatorio responde
`400` indicando cuál.

### Registrar un evento (HU-02)

`POST /api/shipments/{trackingNumber}/events`

```json
{ "tipo": "DISPATCHED", "punto": "Centro de distribución Medellín" }
```

Responde `202`. Con un número inexistente, `404`.

### Consultar el estado (HU-03)

`GET /api/tracking/{trackingNumber}` → estado actual, y punto y fecha del último movimiento.

`GET /api/shipments/{trackingNumber}/events` → historial completo.

### Ver el flujo de eventos

Lo más ilustrativo es tener abierta la consola de RabbitMQ (http://localhost:15672) mientras se
registra un envío: el mensaje entra y sale de `shipments.requests.queue`, y acto seguido las
tablas de los tres módulos quedan pobladas sin que ninguna petición HTTP las haya escrito.

## Despliegue

El repositorio incluye [`Dockerfile`](../Dockerfile) (multi-etapa, compila con Maven y empaqueta
sobre un JRE 21) y [`render.yaml`](../render.yaml) para desplegar en Render como *Blueprint*.

Requisitos del entorno: una base PostgreSQL accesible por internet y un RabbitMQ gestionado.
Las variables de la tabla anterior se definen en el panel del proveedor, nunca en el repositorio.

Verificación del despliegue: `/actuator/health` responde `{"status":"UP"}` solo si la base de
datos **y** el broker están conectados.
