package com.uceshop.catalog.repo;

import com.uceshop.catalog.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {}
