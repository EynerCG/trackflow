package com.trackflow.modules.logistics.domain;

/**
 * En qué ciudad debe estar el centro donde se registra un evento, según su tipo y el
 * punto del recorrido en que va el envío.
 *
 * Es lo que permite que el operador vea solo los centros que tienen sentido en vez
 * del catálogo entero: la misma regla que valida el registro alimenta la lista que se
 * le ofrece, así nunca se le muestra una opción que el backend vaya a rechazar.
 */
public enum CiudadEsperada {

    /** La ciudad de origen del envío: el primer recibido en centro ocurre ahí. */
    ORIGEN("la ciudad de origen del envío"),

    /** La ciudad de destino: llegada al centro destino, salida a reparto y entrega. */
    DESTINO("la ciudad de destino del envío"),

    /** Donde está el paquete ahora: se despacha desde el centro en el que está. */
    ACTUAL("la ciudad donde está el paquete"),

    /**
     * Cualquier ciudad menos la de destino. Un envío puede pasar por hubs
     * intermedios, pero si el centro ya es de la ciudad de destino el movimiento no
     * es un paso más: es la llegada, y le corresponde ARRIVED_AT_DESTINATION_CENTER.
     */
    HUB_INTERMEDIO("una ciudad distinta a la de destino");

    private final String descripcion;

    CiudadEsperada(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() {
        return descripcion;
    }
}
