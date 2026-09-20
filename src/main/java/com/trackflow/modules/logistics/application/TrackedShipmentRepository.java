package com.trackflow.modules.logistics.application;

import com.trackflow.modules.logistics.domain.TrackedShipment;
import java.util.Optional;

public interface TrackedShipmentRepository {

    TrackedShipment save(TrackedShipment trackedShipment);

    boolean exists(String trackingNumber);

    /** Trae el envío conocido con sus ciudades, para las reglas de coherencia del centro. */
    Optional<TrackedShipment> porTrackingNumber(String trackingNumber);
}
