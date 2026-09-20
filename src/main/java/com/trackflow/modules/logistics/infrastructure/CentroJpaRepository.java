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
    @Query("""
            select c from Centro c
            where (:texto is null or upper(c.name) like upper(concat('%', :texto, '%')))
              and (:ciudadId is null or c.cityId = :ciudadId)
              and c.active = true
            order by case when :texto is not null and upper(c.name) like upper(concat(:texto, '%'))
                          then 0 else 1 end,
                     c.name
            """)
    List<Centro> buscar(@Param("texto") String texto, @Param("ciudadId") Long ciudadId, Limit limite);
}
