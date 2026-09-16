# ADR-004 — Puertos en `application`, adaptadores en `infrastructure`

- **Estado:** Aceptada
- **Fecha:** 2026-09-15
- **Responsable:** Eyner Gómez Quintero

## Contexto

Cada módulo se organiza en cuatro capas: `api`, `application`, `domain` e `infrastructure`.
Faltaba definir dónde se declaran las interfaces de acceso a recursos externos —repositorios,
publicadores de eventos, generadores de identificadores— y dónde sus implementaciones.

La propuesta inicial ubicaba tanto la interfaz `ShipmentRepository` como su implementación
dentro de `infrastructure`, que es una estructura frecuente en proyectos Spring.

El problema: si la interfaz vive en `infrastructure`, entonces `application` debe importar
`infrastructure` para usarla, y la dependencia apunta hacia afuera. Eso invierte la regla de
dependencia de Clean Architecture y hace que los casos de uso queden atados a la tecnología.

## Alternativas consideradas

**A. Interfaz e implementación en `infrastructure`.** Agrupa todo lo relacionado con
persistencia en un mismo sitio y es cómodo de navegar. Pero acopla `application` a
`infrastructure` y hace imposible probar un caso de uso sin arrastrar JPA.

**B. Usar directamente las interfaces de Spring Data.** Inyectar `JpaRepository` en los casos de
uso, sin interfaz propia. Es lo más corto de escribir, pero mete la tecnología de persistencia
dentro de la lógica de negocio: los casos de uso pasan a hablar el lenguaje de Spring Data.

**C. Puerto en `application`, adaptador en `infrastructure`.** La interfaz expresa lo que el
caso de uso necesita, en su propio lenguaje; la implementación traduce a la tecnología concreta.

## Decisión

Se adopta la alternativa **C**.

Las interfaces se declaran en `application` y las implementaciones en `infrastructure`. Por
ejemplo, `ShipmentRepository` (puerto) y `JpaShipmentRepository` (adaptador), o
`EventoLogisticoPublisher` y `RabbitMQEventoLogisticoPublisher`.

## Consecuencias

**Positivas**

- La dependencia apunta hacia adentro: `infrastructure` depende de `application`, nunca al revés.
- Los casos de uso se pueden probar con dobles de prueba, sin base de datos ni broker.
- Cambiar la tecnología es escribir otro adaptador. Esto no fue teórico: al añadir la ingesta
  por RabbitMQ (ADR-002) no hubo que tocar ni el dominio ni los casos de uso, solo agregar un
  adaptador de entrada junto al REST existente.
- Los puertos documentan lo que cada caso de uso necesita del exterior.

**Negativas**

- Más archivos: por cada repositorio hay una interfaz, un adaptador y la interfaz de Spring Data.
- Para quien no conoce el patrón, la indirección puede parecer innecesaria al principio.

**Seguimiento**

`LogisticsEventController` inyectaba el repositorio directamente para devolver el historial,
saltándose la capa de casos de uso. Se corrigió con `ConsultarHistorial`, y el arreglo destapó
un defecto que la inconsistencia escondía: al no pasar por un caso de uso, nadie comprobaba que
el envío existiera, así que un número inexistente devolvía una lista vacía con 200 en lugar de
404 — indistinguible de un envío sin movimientos.
