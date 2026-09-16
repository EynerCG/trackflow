-- Consultas de diagnóstico de TrackFlow.
-- Se ejecutan contra la base de datos (por ejemplo, el editor SQL de Supabase).
-- Son de solo lectura: ninguna modifica datos ni esquema.

-- ---------------------------------------------------------------------------
-- 1. Volumen por tabla
--
-- Los conteos de shipments, logistics_tracked_shipments y reports_shipment_tracking
-- deberían coincidir: cada envío registrado genera una fila en las tres, por eventos.
-- Si no coinciden, alguna proyección se quedó atrás.
-- ---------------------------------------------------------------------------
select 'shipments' as tabla, count(*) from public.shipments
union all select 'logistics_events', count(*) from public.logistics_events
union all select 'logistics_tracked_shipments', count(*) from public.logistics_tracked_shipments
union all select 'reports_shipment_tracking', count(*) from public.reports_shipment_tracking;


-- ---------------------------------------------------------------------------
-- 2. Proyecciones desincronizadas  ← la más importante
--
-- Compara el estado real del envío con el que expone la consulta de HU-03.
-- Debe devolver CERO filas. Si devuelve alguna, la proyección de reports quedó
-- desfasada y se repara con POST /api/admin/reconstruir-proyecciones (rol ADMIN).
--
-- Esta consulta es la detección que el sistema no hace por sí solo: ver ADR-005.
-- ---------------------------------------------------------------------------
select s.tracking_number,
       s.status as estado_real,
       r.status as estado_proyectado
from public.shipments s
left join public.reports_shipment_tracking r on r.tracking_number = s.tracking_number
where r.tracking_number is null or r.status <> s.status;


-- ---------------------------------------------------------------------------
-- 3. Envíos que logistics no conoce
--
-- Si aparece alguno, ese envío rechazaría eventos con "no existe" aunque esté
-- registrado. Misma causa y misma reparación que la consulta anterior.
-- ---------------------------------------------------------------------------
select s.tracking_number
from public.shipments s
left join public.logistics_tracked_shipments t on t.tracking_number = s.tracking_number
where t.tracking_number is null;


-- ---------------------------------------------------------------------------
-- 4. Historial de un envío
-- ---------------------------------------------------------------------------
select tracking_number, type, point, registered_at
from public.logistics_events
where tracking_number = 'TF000000000002'
order by registered_at;


-- ---------------------------------------------------------------------------
-- 5. Envíos estancados
--
-- Registrados hace más de un día y sin ningún movimiento: son los que generan
-- llamadas a servicio al cliente, que es el problema que describe el caso de negocio.
-- ---------------------------------------------------------------------------
select tracking_number, status, registered_at, last_movement_at
from public.reports_shipment_tracking
where last_movement_at is null
  and registered_at < now() - interval '1 day'
order by registered_at;


-- ---------------------------------------------------------------------------
-- 6. Estado de las migraciones
--
-- La primera fila debe ser el baseline (bases creadas antes de adoptar Flyway)
-- o la migración inicial. 'success' en false significa una migración fallida.
-- ---------------------------------------------------------------------------
select installed_rank, version, description, type, success, installed_on
from public.flyway_schema_history
order by installed_rank;
