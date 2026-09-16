package com.trackflow.modules.logistics.application;

import com.trackflow.modules.logistics.domain.LogisticsEvent;
import java.util.List;

public interface LogisticsEventRepository {

    LogisticsEvent save(LogisticsEvent event);

    List<LogisticsEvent> findHistorial(String trackingNumber);

    boolean existePorEventId(String eventId);
}
