package com.trackflow.modules.shipments.infrastructure;

import com.trackflow.modules.shipments.domain.Shipment;
import com.trackflow.modules.shipments.domain.TrackingNumber;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentJpaRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByTrackingNumber(TrackingNumber trackingNumber);

    boolean existsByTrackingNumber(TrackingNumber trackingNumber);
}
