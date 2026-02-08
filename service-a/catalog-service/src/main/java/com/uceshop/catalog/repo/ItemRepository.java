package com.uceshop.catalog.repo;

import com.uceshop.catalog.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Reserva stock de forma atómica:
     * - solo descuenta si quantity >= qty
     * - devuelve 1 si actualizó, 0 si no hubo stock o no existe
     */
    @Modifying
    @Transactional
    @Query("""
        update Item i
           set i.quantity = i.quantity - :qty
         where i.id = :id
           and i.quantity >= :qty
    """)
    int reserveStock(@Param("id") Long id, @Param("qty") int qty);
}
