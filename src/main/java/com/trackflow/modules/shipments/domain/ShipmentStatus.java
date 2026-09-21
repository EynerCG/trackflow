package com.trackflow.modules.shipments.domain;

/**
 * Las seis etapas del recorrido, en orden.
 *
 * El centro de destino tiene estado propio aunque físicamente también sea "estar en
 * un centro": mientras los dos compartieron AT_DISTRIBUTION_CENTER, llegar a la ciudad
 * de destino parecía un retroceso al paso anterior para cualquiera que dibujara el
 * progreso a partir del estado. Con seis valores distintos para seis etapas, avanzar
 * es lo único que puede pasar.
 */
public enum ShipmentStatus {
    REGISTERED,
    AT_DISTRIBUTION_CENTER,
    IN_TRANSIT,
    AT_DESTINATION_CENTER,
    OUT_FOR_DELIVERY,
    DELIVERED
}
