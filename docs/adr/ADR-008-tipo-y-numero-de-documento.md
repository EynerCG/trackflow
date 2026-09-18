# ADR-008 — El documento de identidad se guarda como tipo y número

- **Estado:** Aceptada
- **Fecha:** 2026-09-17
- **Responsable:** Eyner Gómez Quintero

## Contexto

`Party` guardaba el documento del remitente y del destinatario en un solo campo de texto
(`documento`), y los datos semilla lo llenaban con valores como `"CC1001"`.

Ese valor mezcla dos datos distintos: el **tipo** de documento y su **número**. Al estar
concatenados, el sistema no puede hacer nada con ninguno de los dos:

- No se puede validar. Una cédula colombiana son entre 6 y 10 dígitos; un pasaporte es
  alfanumérico; un NIT trae dígito de verificación. Sin saber el tipo no hay regla que aplicar, y
  `"CC1001"` —una cédula de cuatro dígitos con el prefijo pegado— entraba sin objeción.
- No se puede agrupar. Una pregunta operativa normal en una transportadora es cuántos envíos
  despachan empresas (`NIT`) frente a personas naturales; sobre un texto libre eso exige
  adivinar el prefijo con `LIKE`.
- No hay catálogo. Nada impedía escribir `"cc 1001"`, `"C.C. 1001"` o `"1001"` para la misma
  persona.

Las transportadoras reales —FedEx, Servientrega, Interrapidísimo— piden el tipo y el número por
separado en su formulario de remisión, precisamente porque el tipo es el que determina cómo se
valida e identifica al cliente.

## Alternativas consideradas

**A. Dejar el campo único y validar por convención de prefijo.** Sin cambios de esquema ni de
contrato. A cambio, la regla de negocio queda escondida en expresiones regulares sobre texto
libre y cualquier variación de escritura la rompe.

**B. Tipo y número como dos campos, con el tipo restringido a un catálogo.** Obliga a migrar los
datos y a cambiar el contrato de la API, pero hace explícito un dato que el negocio ya trataba
como obligatorio.

**C. Una tabla `tipo_documento` con clave foránea.** Es lo correcto para un catálogo que crece o
que lleva atributos propios. Aquí son cinco valores que no cambian dentro del producto y
`shipments` es el único que los usa; una tabla añadiría un `JOIN` a cada consulta sin ganancia.

## Decisión

Se adopta la alternativa **B**.

`Party` pasa a tener `documentType` (enum `TipoDocumento`: `CC`, `CE`, `TI`, `PP`, `NIT`) y
`documentNumber`. En la base son dos columnas por persona, con un `CHECK` que restringe el tipo
al catálogo.

**La validación vive en el constructor de `Party`, no en el DTO.** Es deliberado: al envío se
puede llegar por REST o por un mensaje de RabbitMQ, y una regla puesta en el DTO solo protegería
el primer camino. Cada tipo declara el formato que acepta y, cuando el NIT llega con dígito de
verificación, se comprueba con las ponderaciones de la DIAN.

La migración `V2__tipo_de_documento_separado.sql` renombra las columnas de número y añade las de
tipo. A las filas existentes se les asigna `CC`, el tipo mayoritario en envíos entre personas
naturales; el número se conserva tal cual, con prefijo incluido, porque inventar una limpieza
sobre datos de prueba no aporta nada.

## Consecuencias

**Positivas**

- Un número mal formado se rechaza con `400` y un mensaje que dice el formato esperado, en vez
  de quedar guardado.
- La regla protege los dos caminos de entrada: un mensaje inválido en la cola va a la DLQ en
  lugar de contaminar la tabla.
- `select sender_document_type, count(*) ... group by 1` responde directo cuántos envíos vienen
  de empresas.
- El `CHECK` impide que un `insert` manual meta un tipo fuera del catálogo.

**Negativas**

- **Cambia el contrato de la API.** `documento` desaparece y en su lugar van `tipoDocumento` y
  `numeroDocumento`; las peticiones con el formato anterior responden `400`. Hay que avisarle a
  quien esté escribiendo las pruebas antes de que las dé por buenas.
- Los datos migrados quedan con un tipo asumido (`CC`) que puede no ser el real. Es aceptable
  porque solo son datos semilla.
- Si más adelante se necesita un tipo nuevo, hay que tocar el enum **y** el `CHECK`: dos sitios
  en lugar de una fila en una tabla. Es el precio de la alternativa B sobre la C.
