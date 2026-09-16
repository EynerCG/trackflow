package com.trackflow.modules.reports.infrastructure;

import com.trackflow.modules.reports.application.ProyectarSeguimientoEnvio;
import com.trackflow.shared.events.EnvioCreadoEvent;
import com.trackflow.shared.events.EventoLogisticoRegistradoEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SeguimientoProjectionSubscriber {

    private final ProyectarSeguimientoEnvio proyeccion;

    public SeguimientoProjectionSubscriber(ProyectarSeguimientoEnvio proyeccion) {
        this.proyeccion = proyeccion;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(EnvioCreadoEvent event) {
        proyeccion.alCrearseElEnvio(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(EventoLogisticoRegistradoEvent event) {
        proyeccion.alRegistrarseUnEvento(event);
    }
}
