# Registros de decisiones de arquitectura (ADR)

Decisiones con impacto estructural tomadas durante el Sprint 1, en orden de prioridad: las
primeras condicionan a las siguientes.

| ADR | Decisión | Estado |
|---|---|---|
| [ADR-001](ADR-001-monolito-modular.md) | Monolito modular en lugar de microservicios | Aceptada |
| [ADR-002](ADR-002-ingesta-por-eventos.md) | La información entra por eventos; el REST solo publica | Aceptada |
| [ADR-003](ADR-003-eventos-de-integracion-compartidos.md) | Eventos de integración en `shared/events` | Aceptada |
| [ADR-004](ADR-004-puertos-y-adaptadores.md) | Puertos en `application`, adaptadores en `infrastructure` | Aceptada |
| [ADR-005](ADR-005-modelo-de-lectura-para-consultas.md) | Modelo de lectura propio para la consulta de estado | Aceptada |
| [ADR-006](ADR-006-flyway-para-el-esquema.md) | Flyway como dueño del esquema de base de datos | Aceptada |
| [ADR-007](ADR-007-autenticacion-con-jwt.md) | Autenticación de operadores con JWT | Aceptada |
| [ADR-008](ADR-008-tipo-y-numero-de-documento.md) | El documento se guarda como tipo y número | Aceptada |
| [ADR-009](ADR-009-fecha-de-ocurrencia-del-movimiento.md) | El movimiento guarda cuándo ocurrió, no solo cuándo se reportó | Aceptada |

Cada ADR registra contexto, alternativas consideradas, decisión, consecuencias, responsable y
fecha. Un ADR no se edita cuando cambia de opinión el equipo: se escribe uno nuevo que
reemplace al anterior.
