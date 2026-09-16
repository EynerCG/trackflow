package com.trackflow.modules.logistics.infrastructure;

import com.trackflow.modules.logistics.application.SeguirEnvioCreado;
import com.trackflow.shared.events.EnvioCreadoEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EnvioCreadoSubscriber {

    private final SeguirEnvioCreado seguirEnvioCreado;

    public EnvioCreadoSubscriber(SeguirEnvioCreado seguirEnvioCreado) {
        this.seguirEnvioCreado = seguirEnvioCreado;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(EnvioCreadoEvent event) {
        seguirEnvioCreado.ejecutar(event);
    }
}
