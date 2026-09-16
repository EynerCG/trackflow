package com.trackflow.modules.reports.application;

import com.trackflow.modules.reports.domain.ShipmentTrackingView;
import com.trackflow.modules.reports.domain.TrackingNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarEstadoEnvio {

    private final ShipmentTrackingViewRepository views;

    public ConsultarEstadoEnvio(ShipmentTrackingViewRepository views) {
        this.views = views;
    }

    @Transactional(readOnly = true)
    public ShipmentTrackingView ejecutar(String trackingNumber) {
        return views.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new TrackingNotFoundException(trackingNumber));
    }
}
