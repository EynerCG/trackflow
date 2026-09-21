# Flujo del envío y contrato para la interfaz

Cómo avanza un envío por la cadena, qué se valida en cada paso y qué debe pedirle
la interfaz al backend para no tener que decidir nada por su cuenta.

## El recorrido

```
REGISTERED
  └─ RECEIVED_AT_CENTER            (centro de la ciudad de ORIGEN)      → AT_DISTRIBUTION_CENTER
       └─ DISPATCHED               (centro donde está el paquete)       → IN_TRANSIT
            ├─ RECEIVED_AT_CENTER  (hub: cualquier ciudad menos destino)→ AT_DISTRIBUTION_CENTER
            └─ ARRIVED_AT_DESTINATION_CENTER (ciudad de DESTINO)        → AT_DISTRIBUTION_CENTER
                 └─ OUT_FOR_DELIVERY          (ciudad de DESTINO)       → OUT_FOR_DELIVERY
                      └─ DELIVERED            (ciudad de DESTINO)       → DELIVERED  (terminal)
```

Las transiciones se definen sobre el **último movimiento**, no sobre el estado: el
estado no distingue el centro de origen del de destino —los dos dejan el envío en
`AT_DISTRIBUTION_CENTER`— pero desde el primero lo que sigue es despachar y desde el
segundo, salir a reparto.

El estado actual y la ubicación no se guardan en columnas propias: se derivan del
historial de eventos, que ya es la fuente de la trazabilidad. Así no pueden
desincronizarse.

Todo esto vive en `FlujoLogistico` (módulo `logistics`, capa de dominio).

## Qué se rechaza y con qué código

| Situación | HTTP | Título |
|---|---|---|
| Falta `tipo` o `centroId` | 400 | (validación de campos) |
| `ocurridoEn` en el futuro | 400 | Fecha del movimiento inválida |
| El envío no existe | 404 | — |
| `centroId` inexistente o inactivo | 422 | Centro inválido |
| El movimiento no cabe en el recorrido | 422 | Movimiento fuera del recorrido |
| El centro no está en la ciudad que exige la regla | 422 | El centro no está en la ciudad esperada |
| `ocurridoEn` anterior al último movimiento | 422 | Movimiento anterior al último registrado |

Reportar tarde sigue siendo válido: lo que no se admite es reportar un movimiento
como **anterior** a otro ya registrado, porque entonces la máquina estaría validando
contra un movimiento que no es el anterior.

## `GET /api/shipments/{trackingNumber}/acciones`

Lo que el operador puede hacer ahora. La interfaz lo pinta tal cual.

```json
{
  "trackingNumber": "TF000000000002",
  "estadoActual": "IN_TRANSIT",
  "ultimoMovimiento": "DISPATCHED",
  "ultimoMovimientoEn": "2026-09-20T04:00:00Z",
  "ubicacionActual": {
    "centroId": 1,
    "centroNombre": "Centro Norte",
    "ciudadId": 2,
    "ciudadNombre": "MEDELLÍN - ANTIOQUIA"
  },
  "origen":  { "id": 2, "nombre": "MEDELLÍN - ANTIOQUIA" },
  "destino": { "id": 1, "nombre": "BOGOTÁ - CUNDINAMARCA" },
  "acciones": [
    {
      "tipo": "ARRIVED_AT_DESTINATION_CENTER",
      "etiqueta": "Llegada al centro destino",
      "estadoResultante": "AT_DISTRIBUTION_CENTER",
      "ciudadEsperada": "DESTINO",
      "ciudadDeLosCentros": { "id": 1, "nombre": "BOGOTÁ - CUNDINAMARCA" },
      "centros": [
        {
          "id": 3,
          "nombre": "Centro Fontibón",
          "tipo": "CENTRO_DISTRIBUCION",
          "ciudadId": 1,
          "ciudadNombre": "BOGOTÁ - CUNDINAMARCA",
          "etiqueta": "Centro Fontibón · BOGOTÁ - CUNDINAMARCA"
        }
      ],
      "ruta": null
    }
  ]
}
```

Notas para quien construya la pantalla:

- **`acciones` es la lista de botones.** Si viene vacía, el envío está entregado y no
  admite más movimientos.
- **`centros` de cada acción es el desplegable de esa acción**, ya filtrado. No hay
  que llamar a `/api/centros` ni adivinar la ciudad: al marcar "Entregado" solo
  llegan los centros de la ciudad de destino.
- **`ruta`** solo viene en `DISPATCHED`, con el formato `"MEDELLÍN - ANTIOQUIA →
  BOGOTÁ - CUNDINAMARCA"`. Se calcula en el backend desde las entidades; el operador
  no la escribe.
- **`ciudadEsperada`** (`ORIGEN`, `DESTINO`, `ACTUAL`, `HUB_INTERMEDIO`) sirve para
  explicar por qué se ofrecen esos centros y no otros.
- **`ubicacionActual.centroId`** es null mientras el envío no haya pasado por ningún
  centro; la ciudad, en ese caso, es la de origen.

## `POST /api/shipments/{trackingNumber}/events`

```json
{ "tipo": "ARRIVED_AT_DESTINATION_CENTER", "centroId": 3, "observaciones": null, "ocurridoEn": null }
```

`centroId` es **obligatorio**. El campo `punto` de texto libre se retiró: mientras
existió, bastaba mandarlo sin centro para saltarse todas las reglas, porque de un
texto no se puede saber en qué ciudad ocurrió el movimiento.

Responde `202 Accepted`: el evento se encola y lo registra el consumidor. El
historial (`GET .../events`) puede tardar un instante en reflejarlo.
