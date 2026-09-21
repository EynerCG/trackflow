package com.trackflow.bootstrap;

import com.trackflow.modules.logistics.application.CatalogoDeCentros;
import com.trackflow.modules.logistics.application.EventoLogisticoEntrante;
import com.trackflow.modules.logistics.application.EventoLogisticoPublisher;
import com.trackflow.modules.logistics.domain.Centro;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final CatalogoDeCentros centros;
    private final Clock clock;

    public DataSeeder(EnvioSolicitadoPublisher envios, EventoLogisticoPublisher eventos, ShipmentRepository shipments,
            CatalogoDeCiudades ciudades, CatalogoDeCentros centros, Clock clock) {
        this.envios = envios;
        this.eventos = eventos;
        this.shipments = shipments;
        this.ciudades = ciudades;
        this.centros = centros;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        if (existe(SIN_MOVIMIENTOS)) {
            log.info("Datos semilla ya cargados, no se vuelven a crear");
            return;
        }

        solicitarEnvio(SIN_MOVIMIENTOS, "Documentos legales", hace(2));
        solicitarEnvio(EN_TRANSITO, "Repuestos industriales", hace(10));
        solicitarEnvio(ENTREGADO, "Equipo médico", hace(36));

        // Los eventos se rechazan si el envío aún no está registrado, así que hay que
        // esperar a que la cola de solicitudes termine de procesarse.
        esperarA(EN_TRANSITO);
        esperarA(ENTREGADO);

        // Los tres envíos semilla van de Medellín a Bogotá, así que los movimientos
        // usan centros reales de esas dos ciudades y siguen el orden que exige
        // FlujoLogistico: la semilla debe ser un recorrido que el propio sistema
        // habría aceptado, no una secuencia cualquiera.
        Ciudad medellin = buscarCiudad("MEDELLÍN");
        Ciudad bogota = buscarCiudad("BOGOTÁ");
        Centro centroOrigen = buscarCentro("Centro Norte", medellin.id());
        Centro centroDestino = buscarCentro("Centro Fontibón", bogota.id());

        // En tránsito: entró al centro de origen y salió hacia el destino.
        publicarEvento("seed-evt-001", EN_TRANSITO, EventType.RECEIVED_AT_CENTER, centroOrigen, medellin, hace(9));
        publicarEvento("seed-evt-002", EN_TRANSITO, EventType.DISPATCHED, centroOrigen, medellin, hace(8));

        // Entregado: el recorrido completo, de punta a punta.
        publicarEvento("seed-evt-003", ENTREGADO, EventType.RECEIVED_AT_CENTER, centroOrigen, medellin, hace(35));
        publicarEvento("seed-evt-004", ENTREGADO, EventType.DISPATCHED, centroOrigen, medellin, hace(33));
        publicarEvento("seed-evt-005", ENTREGADO, EventType.ARRIVED_AT_DESTINATION_CENTER, centroDestino, bogota,
                hace(9));
        publicarEvento("seed-evt-006", ENTREGADO, EventType.OUT_FOR_DELIVERY, centroDestino, bogota,
                "Carlos Repartidor", hace(5));
        publicarEvento("seed-evt-007", ENTREGADO, EventType.DELIVERED, centroDestino, bogota, hace(2));

        log.info("Datos semilla encolados: {} (sin movimientos), {} (en tránsito), {} (entregado)",
                SIN_MOVIMIENTOS, EN_TRANSITO, ENTREGADO);
    }

    /**
     * Los movimientos se escalonan en el pasado en vez de compartir el instante de
     * arranque: el historial se ordena por fecha de ocurrencia, y con marcas
     * idénticas no habría forma de saber cuál fue el último.
     */
    private Instant hace(int horas) {
        return clock.instant().minus(horas, ChronoUnit.HOURS);
    }

    private void solicitarEnvio(String trackingNumber, String descripcion, Instant registradoEn) {
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
                registradoEn));
    }

    private Ciudad buscarCiudad(String nombre) {
        return ciudades.buscar(nombre, 1).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo de ciudades no tiene '%s'; revise la migración V4".formatted(nombre)));
    }

    private Centro buscarCentro(String nombre, Long ciudadId) {
        return centros.buscar(nombre, ciudadId, 1).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "El catálogo de centros no tiene '%s' en la ciudad %d; revise la migración V6"
                                .formatted(nombre, ciudadId)));
    }

    /** Resuelve el nombre y la ciudad del centro como lo haría AdmitirEventoLogistico. */
    private void publicarEvento(String eventId, String trackingNumber, EventType tipo, Centro centro,
            Ciudad ciudadCentro, Instant ocurridoEn) {
        publicarEvento(eventId, trackingNumber, tipo, centro, ciudadCentro, null, ocurridoEn);
    }

    /** Variante con repartidor, para el único evento del seed que lo exige: OUT_FOR_DELIVERY. */
    private void publicarEvento(String eventId, String trackingNumber, EventType tipo, Centro centro,
            Ciudad ciudadCentro, String repartidorNombre, Instant ocurridoEn) {
        eventos.publicar(new EventoLogisticoEntrante(
                eventId, trackingNumber, tipo, centro.getId(), centro.getName(), ciudadCentro.etiqueta(), null,
                repartidorNombre, ocurridoEn));
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
