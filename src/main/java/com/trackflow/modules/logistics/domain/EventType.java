package com.trackflow.modules.logistics.domain;

public enum EventType {
    RECEIVED_AT_CENTER("AT_DISTRIBUTION_CENTER"),
    DISPATCHED("IN_TRANSIT"),
    ARRIVED_AT_DESTINATION_CENTER("AT_DISTRIBUTION_CENTER"),
    OUT_FOR_DELIVERY("OUT_FOR_DELIVERY"),
    DELIVERED("DELIVERED");

    private final String resultingStatus;

    EventType(String resultingStatus) {
        this.resultingStatus = resultingStatus;
    }

    public String resultingStatus() {
        return resultingStatus;
    }
}
