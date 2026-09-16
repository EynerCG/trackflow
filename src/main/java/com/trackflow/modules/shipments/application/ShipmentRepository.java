package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.Shipment;
import com.trackflow.modules.shipments.domain.TrackingNumber;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository {

    Shipment save(Shipment shipment);

    List<Shipment> findAll();

    Optional<Shipment> findByTrackingNumber(TrackingNumber trackingNumber);

    boolean existsByTrackingNumber(TrackingNumber trackingNumber);
}
