# ADR-010 — La ciudad sale de un catálogo, no de un campo de texto

- **Estado:** Aceptada
- **Fecha:** 2026-09-18
- **Responsable:** Eyner Gómez Quintero

## Contexto

La ciudad de origen y la de destino eran texto libre dentro de `Party`. Quien registraba el envío
escribía el nombre a mano, así que "Bogotá", "Bogota", "BOGOTA D.C." y "Bobota" eran cuatro
ciudades distintas para el sistema, y las tres primeras la misma para cualquier persona.

Las consecuencias no son cosméticas:

- No se puede contar cuántos envíos van a una ciudad, que es la primera pregunta de cualquier
  informe de operación.
- No se puede saber si hay cobertura en el destino antes de admitir el envío.
- Un error de digitación no lo detecta nadie hasta que el paquete está en la bodega equivocada.

Las transportadoras resuelven esto con un catálogo y un autocompletado: se escribe "bogo" y se
escoge de una lista, no se teclea el nombre.

## Alternativas consideradas

**A. Normalizar el texto** (mayúsculas, sin tildes) y seguir con texto libre. Junta "Bogotá" con
"bogota", pero no detecta "Bobota" ni permite saber si hay cobertura.

**B. Catálogo con identificador propio**, y el envío guarda ese identificador.

**C. Catálogo identificado por código DANE.** Es el estándar nacional y el que se necesita para
interoperar con facturación electrónica o con otra transportadora. A cambio obliga a cargar y
mantener correctos los 1.122 municipios oficiales; un código equivocado es peor que no tenerlo.

## Decisión

Se adopta la alternativa **B**.

Se crea la tabla `cities` con identificador propio y `unique (nombre, departamento)`, que ya
identifica el municipio sin ambigüedad. El envío guarda **solo el identificador**, con clave
foránea al catálogo: repetir el nombre y el departamento dentro del envío sería una dependencia
transitiva y rompería 3NF.

El código DANE no se adopta ahora. Si más adelante hace falta, entra como columna nullable y
única sin tocar nada de lo demás.

`GET /api/ciudades?q=bogo` alimenta el autocompletado, con las coincidencias que empiezan por el
texto primero. El cliente envía `ciudadId`, nunca el nombre.

El catálogo vive en `shared/geografia`, no en un módulo. Es dato de referencia del que ningún
módulo de negocio es dueño: `shipments` lo usa para origen y destino y `reports` para la etiqueta
que ve el cliente. Es el mismo criterio de [ADR-003](ADR-003-eventos-de-integracion-compartidos.md),
y meterlo en `shipments` habría obligado a `reports` a importarlo.

La clave foránea de `shipments` hacia `cities` **no contradice** la regla de no tener claves
foráneas entre módulos ([5. Modelo de datos](../5-modelo-datos.md)): esa regla protege de acoplar
dos módulos de negocio, y el catálogo no es uno.

En el modelo de lectura sí se guarda la etiqueta ("MEDELLÍN - ANTIOQUIA") junto al identificador.
Ahí la duplicación es intencional y es la razón de ser de la proyección
([ADR-005](ADR-005-modelo-de-lectura-para-consultas.md)): responder la consulta sin unir tablas.

## Consecuencias

**Positivas**

- Se acabaron las variantes del mismo nombre: una ciudad, una fila.
- `group by destination_city_id` responde cuántos envíos van a cada destino.
- Una ciudad fuera del catálogo se rechaza con `400` al admitir el envío, antes de encolarlo.
- Cuando existan cobertura, tiempos de entrega o tarifas por ciudad, tienen dónde colgarse.

**Negativas**

- **Cambia el contrato de la API.** `ciudad` como texto desaparece y en su lugar va `ciudadId`.
  Quien registre un envío tiene que consultar el catálogo primero.
- El catálogo arranca con 57 municipios: las capitales y los de mayor volumen. Un destino que no
  esté obliga a una migración nueva antes de poder despachar allá.
- La migración de los datos existentes resuelve el nombre contra el catálogo comparando sin
  tildes ni mayúsculas. **Lo que no logra resolver detiene la migración** en lugar de inventar una
  ciudad, así que hay que limpiar los datos sucios antes de desplegar.
