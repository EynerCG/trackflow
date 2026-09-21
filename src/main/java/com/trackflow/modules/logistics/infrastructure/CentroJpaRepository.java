package com.trackflow.modules.logistics.infrastructure;

import com.trackflow.modules.logistics.domain.Centro;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface CentroJpaRepository extends JpaRepository<Centro, Long> {

    /**
     * texto y ciudadId son independientes: se puede buscar solo por texto, solo por
     * ciudad, o combinando los dos. Solo devuelve centros activos — uno inactivo no
     * sirve para registrar un evento nuevo, así que tampoco tiene sentido ofrecerlo
     * en el autocompletado.
     */
    /**
     * El cast de :texto no es adorno. Cuando llega null, Postgres no tiene de dónde
     * deducir el tipo del parámetro dentro de upper(...) y lo asume bytea, con lo que
     * la consulta ni siquiera se planifica ("function upper(bytea) does not exist").
     * Decirle que es texto basta para que la rama del null funcione.
     */
    @Query("""
            select c from Centro c
            where (cast(:texto as String) is null
                   or upper(c.name) like upper(concat('%', cast(:texto as String), '%')))
              and (:ciudadId is null or c.cityId = :ciudadId)
              and c.active = true
            order by case when cast(:texto as String) is not null
                           and upper(c.name) like upper(concat(cast(:texto as String), '%'))
                          then 0 else 1 end,
                     c.name
            """)
    List<Centro> buscar(@Param("texto") String texto, @Param("ciudadId") Long ciudadId, Limit limite);
}
