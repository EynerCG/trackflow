# ADR-001 — Monolito modular en lugar de microservicios

- **Estado:** Aceptada
- **Fecha:** 2026-09-15
- **Responsable:** Eyner Gómez Quintero

## Contexto

El Sprint 1 comprende tres historias de usuario que giran alrededor de una misma entidad: el
envío. El equipo venía de implementar HU-02 como un microservicio independiente
(`ms-eventos-microservicios`), siguiendo el formato del ejemplo del curso, que propone un
monorepo con un servicio por capacidad y base de datos por servicio.

La pregunta era si continuar por esa vía o consolidar el Sprint 1 en un solo despliegue.

Condiciones del equipo: es un proyecto académico con plazos de sprint, sin operación 24/7, sin
necesidades de escalado diferenciado por capacidad, y con los límites del dominio todavía en
discusión (varias preguntas abiertas de las historias siguen sin responder).

## Alternativas consideradas

**A. Microservicios (un servicio por capacidad).** Máxima independencia de despliegue y
escalado. Exige un broker, base de datos por servicio, despliegue y CI por servicio, y
observabilidad distribuida. Sobre todo, exige acertar con los límites del dominio desde el
principio: mover una frontera implica renegociar un contrato entre repositorios.

**B. Monolito por capas.** Paquetes `api`, `service`, `repository` compartidos por todo el
sistema. Es lo más rápido de escribir, pero la lógica de las tres historias termina mezclada en
los mismos paquetes y no deja fronteras que permitan extraer nada después.

**C. Monolito modular.** Un solo despliegue, dividido internamente por dominio de negocio, con
módulos que solo se comunican por eventos.

## Decisión

Se adopta la alternativa **C**.

El sistema se despliega como una sola aplicación, organizada en tres módulos —`shipments`,
`logistics` y `reports`— que no se conocen entre sí y se integran publicando eventos.

## Consecuencias

**Positivas**

- Una sola unidad que construir, desplegar y depurar, lo que se ajusta al ritmo de un sprint.
- El registro de un envío es atómico: no hace falta consistencia eventual dentro de un módulo.
- Los límites entre módulos quedan explícitos y verificables, de modo que extraer uno a un
  microservicio más adelante es cambiar el transporte de sus eventos, no reescribir su lógica.
- Equivocarse en una frontera cuesta mover un paquete, no renegociar un contrato entre repos.

**Negativas**

- No hay escalado independiente por capacidad: se escala toda la aplicación o ninguna.
- Un fallo grave afecta a las tres historias a la vez.
- La disciplina modular no la impone la infraestructura, sino el equipo: nada a nivel de
  despliegue impide que alguien acople dos módulos si no se vigila en revisión de código.

**Deuda asumida**

Conviene incorporar una verificación automática de dependencias entre módulos (ArchUnit o
Spring Modulith) que falle la construcción si se cruza una frontera. Durante el sprint se
comprobó manualmente que no existen importaciones entre módulos.
