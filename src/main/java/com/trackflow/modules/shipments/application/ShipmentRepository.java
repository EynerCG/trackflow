package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.Shipment;
import com.trackflow.modules.shipments.domain.TrackingNumber;
import java.util.Optional;

public interface ShipmentRepository {

    Shipment save(Shipment shipment);

    Optional<Shipment> findByTrackingNumber(TrackingNumber trackingNumber);

    boolean existsByTrackingNumber(TrackingNumber trackingNumber);
}
