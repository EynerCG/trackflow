package com.trackflow.bootstrap;

import com.trackflow.modules.logistics.application.EventoLogisticoEntrante;
import com.trackflow.modules.logistics.application.EventoLogisticoPublisher;
import com.trackflow.modules.logistics.domain.EventType;
import com.trackflow.modules.shipments.application.EnvioSolicitado;
import com.trackflow.modules.shipments.application.EnvioSolicitadoPublisher;
import com.trackflow.modules.shipments.application.ShipmentRepository;
import com.trackflow.modules.shipments.domain.Party;
import com.trackflow.modules.shipments.domain.TipoDocumento;
import com.trackflow.modules.shipments.domain.TrackingNumber;
import com.trackflow.shared.geografia.CatalogoDeCiudades;
import com.trackflow.shared.geografia.Ciudad;
import java.time.Clock;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Carga los envíos semilla del plan de calidad (sección 6.5), que permiten probar
 * HU-02 y HU-03 sin depender de haber ejecutado HU-01 antes.
 *
 * Publica en las mismas colas que cualquier otro productor: los datos de prueba
 * entran por el mismo camino que los reales, sin puerta trasera a la base de datos.
 */
@Component
@ConditionalOnProperty(name = "trackflow.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final Duration ESPERA_MAXIMA = Duration.ofSeconds(15);

    /** Envío recién registrado, sin movimientos. */
    public static final String SIN_MOVIMIENTOS = "TF000000000001";
    /** Envío en tránsito, con historial. */
    public static final String EN_TRANSITO = "TF000000000002";
    /** Envío ya entregado. */
    public static final String ENTREGADO = "TF000000000003";

    private final EnvioSolicitadoPublisher envios;
    private final EventoLogisticoPublisher eventos;
    private final ShipmentRepository shipments;
    private final CatalogoDeCiudades ciudades;
    private final Clock clock;

    public DataSeeder(EnvioSolicitadoPublisher envios, EventoLogisticoPublisher eventos, ShipmentRepository shipments,
            CatalogoDeCiudades ciudades, Clock clock) {
        this.envios = envios;
        this.eventos = eventos;
        this.shipments = shipments;
        this.ciudades = ciudades;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        if (existe(SIN_MOVIMIENTOS)) {
            log.info("Datos semilla ya cargados, no se vuelven a crear");
            return;
        }

        solicitarEnvio(SIN_MOVIMIENTOS, "Documentos legales");
        solicitarEnvio(EN_TRANSITO, "Repuestos industriales");
        solicitarEnvio(ENTREGADO, "Equipo médico");

        // Los eventos se rechazan si el envío aún no está registrado, así que hay que
        // esperar a que la cola de solicitudes termine de procesarse.
        esperarA(EN_TRANSITO);
        esperarA(ENTREGADO);

        publicarEvento("seed-evt-001", EN_TRANSITO, EventType.RECEIVED_AT_CENTER, "Centro de distribución Medellín");
        publicarEvento("seed-evt-002", EN_TRANSITO, EventType.DISPATCHED, "Ruta Medellín - Bogotá");

        publicarEvento("seed-evt-003", ENTREGADO, EventType.RECEIVED_AT_CENTER, "Centro de distribución Cali");
        publicarEvento("seed-evt-004", ENTREGADO, EventType.OUT_FOR_DELIVERY, "Reparto Cali norte");
        publicarEvento("seed-evt-005", ENTREGADO, EventType.DELIVERED, "Dirección del destinatario");

        log.info("Datos semilla encolados: {} (sin movimientos), {} (en tránsito), {} (entregado)",
                SIN_MOVIMIENTOS, EN_TRANSITO, ENTREGADO);
    }

    private void solicitarEnvio(String trackingNumber, String descripcion) {
        // Se buscan por nombre y no por identificador fijo: los ids del catálogo los
        // asigna la migración y no son parte de su contrato.
        Ciudad origen = buscarCiudad("MEDELLÍN");
        Ciudad destino = buscarCiudad("BOGOTÁ");

        envios.publicar(new EnvioSolicitado(
                "seed-env-" + trackingNumber,
                trackingNumber,
                new Party("Ana Remitente", TipoDocumento.CC, "1017254893", "3001112233", "Calle 10 #20-30",
                        origen.id()),
                new Party("Beto Destinatario", TipoDocumento.CC, "79546218", "3004445566", "Carrera 7 #40-50",
                        destino.id()),
                origen,
                destino,
                descripcion,
                clock.instant()));
    }

    private Ciudad buscarCiudad(String nombre) {
        return ciudades.buscar(nombre, 1).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo de ciudades no tiene '%s'; revise la migración V4".formatted(nombre)));
    }

    private void publicarEvento(String eventId, String trackingNumber, EventType tipo, String punto) {
        eventos.publicar(new EventoLogisticoEntrante(
                eventId, trackingNumber, tipo, punto, null, clock.instant()));
    }

    private boolean existe(String trackingNumber) {
        return shipments.existsByTrackingNumber(TrackingNumber.of(trackingNumber));
    }

    private void esperarA(String trackingNumber) throws InterruptedException {
        long limite = System.currentTimeMillis() + ESPERA_MAXIMA.toMillis();
        while (!existe(trackingNumber)) {
            if (System.currentTimeMillis() > limite) {
                log.warn("El envío semilla {} no se registró a tiempo; sus eventos podrían rechazarse",
                        trackingNumber);
                return;
            }
            Thread.sleep(200);
        }
    }
}
