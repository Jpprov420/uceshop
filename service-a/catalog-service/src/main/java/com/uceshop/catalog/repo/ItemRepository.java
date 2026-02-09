package com.uceshop.catalog.repo;

import com.uceshop.catalog.domain.Item;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByName(String name);
    boolean existsByName(String name);

    // ✅ Reserva stock (descuenta) solo si hay suficiente
    @Modifying
    @Transactional
    @Query("update Item i set i.quantity = i.quantity - :qty " +
           "where i.id = :id and i.quantity >= :qty")
    int reserveStock(@Param("id") Long id, @Param("qty") int qty);

    // ✅ Libera stock (suma)
    @Modifying
    @Transactional
    @Query("update Item i set i.quantity = i.quantity + :qty " +
           "where i.id = :id")
    int releaseStock(@Param("id") Long id, @Param("qty") int qty);
}
