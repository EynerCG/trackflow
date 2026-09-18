package com.trackflow.shared.geografia;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface CiudadJpaRepository extends JpaRepository<CiudadRegistrada, Long> {

    /**
     * Las coincidencias que empiezan por el texto van primero: quien escribe "bogo"
     * espera ver "BOGOTÁ D.C." antes que "PUERTO BOGOTÁ".
     */
    @Query("""
            select c from CiudadRegistrada c
            where upper(c.name) like upper(concat('%', :texto, '%'))
               or upper(c.department) like upper(concat('%', :texto, '%'))
            order by case when upper(c.name) like upper(concat(:texto, '%')) then 0 else 1 end,
                     c.name
            """)
    List<CiudadRegistrada> buscar(String texto, Limit limite);
}
