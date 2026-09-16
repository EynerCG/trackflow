package com.trackflow.modules.logistics.infrastructure;

import com.trackflow.modules.logistics.application.LogisticsEventRepository;
import com.trackflow.modules.logistics.domain.LogisticsEvent;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class JpaLogisticsEventRepository implements LogisticsEventRepository {

    private final LogisticsEventJpaRepository jpa;

    public JpaLogisticsEventRepository(LogisticsEventJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public LogisticsEvent save(LogisticsEvent event) {
        return jpa.save(event);
    }

    @Override
    public List<LogisticsEvent> findHistorial(String trackingNumber) {
        return jpa.findByTrackingNumberOrderByRegisteredAtAsc(trackingNumber);
    }
}
