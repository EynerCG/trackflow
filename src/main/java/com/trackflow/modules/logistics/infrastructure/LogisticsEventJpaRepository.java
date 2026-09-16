package com.trackflow.modules.logistics.infrastructure;

import com.trackflow.modules.logistics.domain.LogisticsEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogisticsEventJpaRepository extends JpaRepository<LogisticsEvent, Long> {

    List<LogisticsEvent> findByTrackingNumberOrderByRegisteredAtAsc(String trackingNumber);

    boolean existsByEventId(String eventId);
}
