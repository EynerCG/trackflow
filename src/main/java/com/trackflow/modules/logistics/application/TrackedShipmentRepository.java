package com.trackflow.modules.logistics.application;

import com.trackflow.modules.logistics.domain.TrackedShipment;

public interface TrackedShipmentRepository {

    TrackedShipment save(TrackedShipment trackedShipment);

    boolean exists(String trackingNumber);
}
