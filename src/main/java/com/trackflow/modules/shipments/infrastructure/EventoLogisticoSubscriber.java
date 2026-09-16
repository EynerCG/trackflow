package com.trackflow.modules.shipments.infrastructure;

import com.trackflow.modules.shipments.application.AplicarEventoLogistico;
import com.trackflow.shared.events.EventoLogisticoRegistradoEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EventoLogisticoSubscriber {

    private final AplicarEventoLogistico aplicarEventoLogistico;

    public EventoLogisticoSubscriber(AplicarEventoLogistico aplicarEventoLogistico) {
        this.aplicarEventoLogistico = aplicarEventoLogistico;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(EventoLogisticoRegistradoEvent event) {
        aplicarEventoLogistico.ejecutar(event);
    }
}
