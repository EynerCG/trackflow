package com.trackflow.modules.shipments.domain;

/**
 * El número de documento no corresponde al tipo declarado.
 */
public class DocumentoInvalidoException extends RuntimeException {

    private final TipoDocumento tipo;
    private final String numero;

    public DocumentoInvalidoException(TipoDocumento tipo, String numero) {
        super(tipo == null
                ? "Falta el tipo de documento: debe ser CC, CE, TI, PP o NIT"
                : "El número '%s' no es válido para un documento de tipo %s: se espera %s"
                        .formatted(numero, tipo, tipo.formatoEsperado()));
        this.tipo = tipo;
        this.numero = numero;
    }

    public TipoDocumento getTipo() {
        return tipo;
    }

    public String getNumero() {
        return numero;
    }
}
