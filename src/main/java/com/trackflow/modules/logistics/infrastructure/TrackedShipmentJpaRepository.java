package com.trackflow.modules.logistics.infrastructure;

import com.trackflow.modules.logistics.domain.TrackedShipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackedShipmentJpaRepository extends JpaRepository<TrackedShipment, String> {
}
