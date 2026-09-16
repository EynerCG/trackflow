# TrackFlow — Sistema de tracking logístico

Plataforma para registrar envíos, capturar los eventos de su paso por la cadena logística
y consultar su estado con un número de seguimiento.

Proyecto de Fábrica Escuela (CodeF@ctory UdeA 2026-2) — Sprint 1.

**Servicio desplegado:** https://trackflow-5enb.onrender.com
(la documentación interactiva se abre en la raíz)

## Qué hace

| Historia | Descripción | Endpoint |
|---|---|---|
| HU-01 | Registrar un envío con remitente y destinatario | `POST /api/shipments` |
| HU-02 | Registrar un evento logístico de un envío | `POST /api/shipments/{trackingNumber}/events` |
| HU-03 | Consultar el estado actual de un envío | `GET /api/tracking/{trackingNumber}` |

## Arquitectura en una frase

Monolito modular con Clean Architecture dentro de cada módulo, donde **toda la información
entra por eventos**: los controladores REST solo validan y publican en RabbitMQ, y quien
escribe en la base de datos son los consumidores de las colas.

```
POST /api/shipments   ──→ shipments.topic ──→ shipments.requests.queue ──→ RegistrarEnvio
POST /api/…/events    ──→ logistics.topic ──→ logistics.events.queue   ──→ RegistrarEventoLogistico
                                                                              │
                                              evento de integración ──────────┤
                                                              ┌───────────────┴──────────────┐
                                                         shipments                       reports
                                                     (estado del envío)          (proyección de consulta)
```

Los tres módulos de negocio —`shipments`, `logistics` y `reports`— no se conocen entre sí:
solo dependen del contrato de eventos en `shared/events`.

## Stack

Java 21 · Spring Boot 4.1.1 · Spring Data JPA · Spring AMQP · PostgreSQL · RabbitMQ ·
springdoc-openapi · Maven · Docker

## Documentación

| Documento | Contenido |
|---|---|
| [1. Enunciado](docs/1-enunciado.md) | Caso de negocio e historias de usuario del sprint |
| [2. Arquitectura](docs/2-arquitectura.md) | Estilo, módulos, capas, flujo de eventos y diagramas |
| [3. Instalación de RabbitMQ](docs/3-instalacion-rabbitmq.md) | Broker en local y gestionado |
| [4. Ejecución](docs/4-ejecucion.md) | Cómo levantar y probar el proyecto |
| [5. Modelo de datos](docs/5-modelo-datos.md) | Tablas por módulo y por qué no hay claves foráneas |
| [ADR](docs/adr/) | Decisiones de arquitectura registradas |

## Arranque rápido

```bash
docker compose up -d
./mvnw spring-boot:run "-Dspring-boot.run.arguments=--trackflow.seed.enabled=true"
```

Luego abre http://localhost:8080 — te lleva a la documentación interactiva.

El detalle está en [4. Ejecución](docs/4-ejecucion.md).
