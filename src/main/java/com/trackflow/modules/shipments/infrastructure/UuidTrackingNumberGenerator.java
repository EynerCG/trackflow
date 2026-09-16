package com.trackflow.modules.shipments.infrastructure;

import com.trackflow.modules.shipments.application.ShipmentRepository;
import com.trackflow.modules.shipments.application.TrackingNumberGenerator;
import com.trackflow.modules.shipments.domain.TrackingNumber;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidTrackingNumberGenerator implements TrackingNumberGenerator {

    private static final int SUFFIX_LENGTH = 12;

    private final ShipmentRepository shipments;

    public UuidTrackingNumberGenerator(ShipmentRepository shipments) {
        this.shipments = shipments;
    }

    @Override
    public TrackingNumber next() {
        TrackingNumber candidate;
        do {
            candidate = TrackingNumber.of(TrackingNumber.PREFIX + UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, SUFFIX_LENGTH)
                    .toUpperCase());
        } while (shipments.existsByTrackingNumber(candidate));

        return candidate;
    }
}
