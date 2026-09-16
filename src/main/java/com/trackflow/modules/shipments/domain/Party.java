package com.trackflow.modules.shipments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Party {

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String documentId;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    protected Party() {
    }

    public Party(String fullName, String documentId, String phone, String address, String city) {
        this.fullName = fullName;
        this.documentId = documentId;
        this.phone = phone;
        this.address = address;
        this.city = city;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }
}
