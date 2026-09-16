package com.trackflow.modules.shipments.infrastructure;

import com.trackflow.modules.shipments.application.ShipmentRepository;
import com.trackflow.modules.shipments.domain.Shipment;
import com.trackflow.modules.shipments.domain.TrackingNumber;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaShipmentRepository implements ShipmentRepository {

    private final ShipmentJpaRepository jpa;

    public JpaShipmentRepository(ShipmentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Shipment save(Shipment shipment) {
        return jpa.save(shipment);
    }

    @Override
    public List<Shipment> findAll() {
        return jpa.findAll();
    }

    @Override
    public Optional<Shipment> findByTrackingNumber(TrackingNumber trackingNumber) {
        return jpa.findByTrackingNumber(trackingNumber);
    }

    @Override
    public boolean existsByTrackingNumber(TrackingNumber trackingNumber) {
        return jpa.existsByTrackingNumber(trackingNumber);
    }
}
