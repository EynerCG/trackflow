package com.trackflow.modules.reports.infrastructure;

import com.trackflow.modules.reports.application.ShipmentTrackingViewRepository;
import com.trackflow.modules.reports.domain.ShipmentTrackingView;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaShipmentTrackingViewRepository implements ShipmentTrackingViewRepository {

    private final ShipmentTrackingViewJpaRepository jpa;

    public JpaShipmentTrackingViewRepository(ShipmentTrackingViewJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ShipmentTrackingView save(ShipmentTrackingView view) {
        return jpa.save(view);
    }

    @Override
    public Optional<ShipmentTrackingView> findByTrackingNumber(String trackingNumber) {
        return jpa.findById(trackingNumber);
    }
}
