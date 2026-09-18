package com.trackflow.modules.shipments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Party {

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento documentType;

    @Column(nullable = false)
    private String documentNumber;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    /**
     * Identificador de la ciudad en el catálogo. Se guarda solo el identificador: el
     * nombre y el departamento viven en el catálogo, y repetirlos aquí rompería 3NF.
     */
    @Column(nullable = false)
    private Long cityId;

    protected Party() {
    }

    public Party(String fullName, TipoDocumento documentType, String documentNumber,
            String phone, String address, Long cityId) {
        String numero = documentNumber == null ? null : documentNumber.trim().toUpperCase();
        if (documentType == null || !documentType.aceptaNumero(numero)) {
            throw new DocumentoInvalidoException(documentType, documentNumber);
        }

        this.fullName = fullName;
        this.documentType = documentType;
        this.documentNumber = numero;
        this.phone = phone;
        this.address = address;
        this.cityId = cityId;
    }

    public String getFullName() {
        return fullName;
    }

    public TipoDocumento getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public Long getCityId() {
        return cityId;
    }
}
