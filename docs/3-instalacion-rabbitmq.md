# 3. Instalación de RabbitMQ

La aplicación no registra nada sin broker: toda la escritura entra por las colas. Hay dos
formas de tenerlo.

## Opción A — Docker (recomendada para desarrollo)

Es la más rápida y no deja servicios instalados en la máquina. El repositorio incluye
[`docker-compose.yml`](../docker-compose.yml) con RabbitMQ y PostgreSQL.

```bash
docker compose up -d rabbitmq
```

Comprobar que quedó arriba:

```bash
docker compose ps
```

La consola de administración queda en http://localhost:15672 con usuario `guest` y
contraseña `guest`.

Para levantar también la base de datos local:

```bash
docker compose up -d
```

## Opción B — Instalación nativa en Windows

1. **Instalar Erlang primero.** RabbitMQ está escrito en Erlang y no arranca sin él.
   Descargar el instalador de Windows desde https://www.erlang.org/downloads y ejecutarlo
   como administrador. Hacerlo *antes* de instalar RabbitMQ.
2. **Instalar RabbitMQ Server** desde https://www.rabbitmq.com/docs/install-windows.
   Queda registrado como servicio de Windows y arranca automáticamente.
3. **Habilitar la consola web.** Abrir *RabbitMQ Command Prompt (sbin dir)* desde el menú
   de inicio y ejecutar:

   ```bash
   rabbitmq-plugins enable rabbitmq_management
   ```

4. **Verificar:**

   ```bash
   Get-Service RabbitMQ
   ```

   Debe aparecer como *Running*, y http://localhost:15672 debe pedir credenciales.

> El usuario `guest` solo acepta conexiones desde `localhost`. Sirve en desarrollo, pero un
> despliegue remoto necesita un usuario propio.

## Broker gestionado (despliegue)

En producción se usa **CloudAMQP**, que ofrece RabbitMQ administrado con plan gratuito:

1. Crear una instancia en https://www.cloudamqp.com — plan **Little Lemur (RabbitMQ)**.
   Cuidado: el plan *Loyal Lemming* es **LavinMQ**, otro broker distinto.
2. Elegir una región cercana a donde se despliegue la aplicación.
3. En la sección **AMQP details** están los datos de conexión: host, user, password y vhost.
   En CloudAMQP el usuario y el vhost suelen coincidir.

Diferencias respecto al broker local, ya contempladas en la configuración:

| Aspecto | Local | Gestionado |
|---|---|---|
| Puerto | 5672 | 5671 (TLS) |
| TLS | desactivado | obligatorio |
| Virtual host | `/` | uno propio por instancia |

## Colas y exchanges

La aplicación los declara sola al arrancar; no hay que crearlos a mano. Al conectarse deben
aparecer cuatro colas:

| Cola | Para qué |
|---|---|
| `shipments.requests.queue` | Solicitudes de registro de envíos |
| `logistics.events.queue` | Eventos logísticos reportados |
| `shipments.requests.dlq` | Solicitudes descartadas |
| `logistics.events.dlq` | Eventos descartados |

Si una cola de descarte acumula mensajes, algo se está rechazando: lo habitual es un evento
sobre un envío que no existe.
