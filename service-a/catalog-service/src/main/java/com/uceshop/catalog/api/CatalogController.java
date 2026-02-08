package com.uceshop.catalog.api;

import com.uceshop.catalog.domain.Item;
import com.uceshop.catalog.repo.ItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final ItemRepository itemRepository;

    public CatalogController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // ✅ EXISTENTE: lista items
    @GetMapping("/items")
    public List<Item> all() {
        return itemRepository.findAll();
    }

    // ✅ NUEVO: obtener item por id (para que order-service muestre nombre/precio en GET /orders)
    @GetMapping("/items/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return itemRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ NUEVO: reserva stock (descuenta quantity)
    @PostMapping("/items/{id}/reserve")
    public ResponseEntity<?> reserve(@PathVariable Long id, @RequestParam int qty) {

        if (qty <= 0) {
            return ResponseEntity.badRequest().body("qty debe ser > 0");
        }

        int updated = itemRepository.reserveStock(id, qty);

        if (updated == 1) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Stock insuficiente o item no existe");
    }
}
