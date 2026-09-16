# ADR-007 — Autenticación de operadores con JWT

- **Estado:** Aceptada
- **Fecha:** 2026-09-16
- **Responsable:** Eyner Gómez Quintero

## Contexto

La API quedó desplegada en internet sin control de acceso: cualquiera con la URL podía
registrar envíos o publicar eventos sobre envíos ajenos —por ejemplo, marcar como entregado un
paquete que no lo está.

Las historias del sprint dejan preguntas abiertas que el dueño de producto no ha respondido:

> ¿Quién puede registrar envíos: cualquier operador o solo el del centro de origen?
> ¿La consulta exige que el cliente se identifique, o basta con tener el número de seguimiento?

Había que decidir sin esas respuestas, y sin bloquear ni la demostración ni las pruebas de
calidad, que están en curso.

## Alternativas consideradas

**A. No implementar nada en este sprint.** Registrar la deuda y esperar las respuestas del
negocio. Coherente, pero deja un servicio público escribible por cualquiera durante semanas.

**B. Clave de API en una cabecera.** Barato y suficiente para impedir escrituras anónimas. No
identifica quién escribe, así que no sirve como base para las reglas por operador o por centro
que el negocio acabará pidiendo.

**C. Autenticación con JWT.** El operador obtiene un token firmado y lo presenta en cada
petición. El token transporta identidad y roles, así que admite reglas más finas cuando lleguen
las respuestas.

## Decisión

Se adopta la alternativa **C**, con dos precisiones:

**La consulta de estado permanece pública.** El número de seguimiento es en sí mismo la
credencial del cliente, como en cualquier servicio de paquetería: pedirle además usuario y
contraseña contradiría el propósito de HU-03, que es evitar que tenga que llamar a servicio al
cliente.

**Sin credenciales configuradas, la protección se desactiva.** En local y en las pruebas no hace
falta token; el arranque lo advierte en el log. En el despliegue las credenciales sí se definen,
de modo que la escritura queda protegida sin entorpecer el trabajo del equipo.

Se usa el soporte nativo de Spring Security (Nimbus) en lugar de añadir una librería de terceros,
con firma simétrica HMAC-SHA256: quien emite y quien valida son la misma aplicación.

**Dos roles, por privilegio mínimo.** `OPERADOR` registra envíos y eventos; `ADMIN` puede además
reconstruir proyecciones (`/api/admin/**`), que es mantenimiento del sistema y no trabajo diario.
El administrador incluye los permisos del operador. Si no se configura clave de administrador,
esos endpoints quedan **inaccesibles para todos**: cerrado por defecto en lugar de abierto.

## Consecuencias

**Positivas**

- El servicio desplegado deja de ser escribible por cualquiera.
- El token lleva identidad y rol, así que las reglas por operador o por centro se podrán añadir
  sin rehacer el mecanismo.
- Al ser sin estado, no hay sesiones que replicar si algún día hay más de una instancia.
- La consulta del cliente sigue siendo de un solo paso, como pide la historia.

**Negativas**

- Un token robado sirve hasta que caduca: no hay revocación. La duración por defecto es de una
  hora para acotar la ventana.
- Los usuarios están en configuración, no en base de datos. No hay alta, baja ni cambio de
  contraseña: eso sería una historia propia.
- Los roles no distinguen entre el operador del centro de origen y el del punto de tránsito, que
  es justo lo que la pregunta abierta acabará precisando. Hoy cualquier operador autenticado
  puede registrar cualquier evento sobre cualquier envío.
- Si el secreto de firma se regenera, los tokens vigentes dejan de valer.

**Lo que esta decisión no resuelve**

Sigue sin responderse **quién** puede hacer **qué**. Hoy cualquier operador autenticado puede
registrar cualquier evento sobre cualquier envío. Cuando el negocio defina las reglas, el lugar
para aplicarlas son los casos de uso de admisión, no la configuración de seguridad.
