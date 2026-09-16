package com.trackflow.modules.shipments.application;

import com.trackflow.modules.shipments.domain.TrackingNumber;

public interface TrackingNumberGenerator {

    TrackingNumber next();
}
