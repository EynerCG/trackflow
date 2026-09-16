# ADR-006 — Flyway como dueño del esquema de base de datos

- **Estado:** Aceptada
- **Fecha:** 2026-09-16
- **Responsable:** Eyner Gómez Quintero

## Contexto

Durante el sprint el esquema lo generaba Hibernate con
`spring.jpa.hibernate.ddl-auto=update`: al arrancar comparaba las entidades con las tablas y
creaba lo que faltaba.

Eso permitió avanzar rápido, pero mostró sus límites en cuanto el proyecto tocó una base real.
Al conectar contra Supabase, el log dejó advertencias como:

```
constraint "uk118pxyyjg4p3tfv23npkxughb" of relation "logistics_events" does not exist, skipping
```

Hibernate intentó un cambio, no pudo aplicarlo y continuó sin fallar. `update` solo sabe
añadir: no modifica tipos, no renombra ni elimina, y no avisa cuando no puede.

El problema de fondo es que **el esquema de cada base es el resultado del historial de arranques
de esa base**. No hay forma de saber en qué versión está, ni de revisar un cambio antes de
aplicarlo, ni de revertirlo.

## Alternativas consideradas

**A. Seguir con `ddl-auto=update`.** Cero trabajo adicional. A cambio, el esquema es
impredecible entre entornos y los cambios destructivos se ignoran en silencio.

**B. Scripts SQL manuales** (`schema.sql` versionado, ejecutado a mano). Da control, pero nadie
sabe qué scripts se aplicaron en cada base y es fácil olvidar uno.

**C. Flyway.** Migraciones versionadas que se aplican en orden, con registro de cuáles ya se
ejecutaron en cada base.

## Decisión

Se adopta la alternativa **C**.

El esquema vive en `src/main/resources/db/migration`, empezando por
`V1__esquema_inicial.sql`. Hibernate pasa a `ddl-auto=validate`: ya no modifica nada, solo
verifica al arrancar que las entidades coincidan con las tablas.

Para las bases creadas antes de esta decisión se usa `spring.flyway.baseline-on-migrate=true`,
que las marca como línea base en lugar de intentar recrear tablas existentes.

## Consecuencias

**Positivas**

- Cualquiera que levante el proyecto obtiene exactamente el mismo esquema.
- Los cambios de esquema se revisan en el pull request como cualquier otro código.
- Queda historial: se sabe qué versión tiene cada base y qué migración introdujo cada cambio.
- `validate` convierte una divergencia silenciosa en un fallo visible al arrancar.

**Negativas**

- Cada cambio de entidad exige escribir su migración. Olvidarla hace que la aplicación **no
  arranque**, porque `validate` lo detecta. Es un fallo ruidoso, que es justo lo que se busca,
  pero cambia la forma de trabajar del equipo.
- Las migraciones aplicadas no se editan: un error se corrige con una migración nueva.
- Un SQL que no coincida con lo que esperan las entidades impide el arranque, así que conviene
  probar contra una base limpia antes de desplegar.
