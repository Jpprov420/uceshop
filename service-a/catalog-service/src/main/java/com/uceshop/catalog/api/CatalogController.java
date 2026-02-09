package com.uceshop.catalog.api;

import com.uceshop.catalog.domain.Item;
import com.uceshop.catalog.repo.ItemRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final ItemRepository repo;

    public CatalogController(ItemRepository repo) {
        this.repo = repo;
    }

    // ✅ GET lista
    @GetMapping("/items")
    public List<Item> all() {
        return repo.findAll();
    }

    // ✅ GET por id
    @GetMapping("/items/{id}")
    public ResponseEntity<?> byId(@PathVariable Long id) {
        return repo.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ POST /catalog/items (NUEVO) con validación anti-duplicados por name
    @PostMapping("/items")
    public ResponseEntity<?> create(@RequestBody CreateItemRequest req) {

        // 1) Validaciones básicas
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("name es obligatorio");
        }
        if (req.getPrice() == null || req.getPrice().signum() <= 0) {
            return ResponseEntity.badRequest().body("price debe ser > 0");
        }
        if (req.getQuantity() == null || req.getQuantity() < 0) {
            return ResponseEntity.badRequest().body("quantity debe ser >= 0");
        }

        String name = req.getName().trim();

        // 2) Validación clave: si ya existe, 409 Conflict
        if (repo.existsByName(name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un item con name='" + name + "'");
        }

        // 3) Guardar
        Item item = new Item();
        item.setName(name);
        item.setPrice(req.getPrice());
        item.setQuantity(req.getQuantity());

        Item saved = repo.save(item);

        // 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ✅ PUT actualizar item
    @PutMapping("/items/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Item req) {
        return repo.findById(id).map(existing -> {
            if (req.getName() != null) existing.setName(req.getName());
            if (req.getPrice() != null) existing.setPrice(req.getPrice());
            if (req.getQuantity() >= 0) existing.setQuantity(req.getQuantity());
            return ResponseEntity.ok(repo.save(existing));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ DELETE item
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ POST reservar stock: /catalog/items/{id}/reserve?qty=2
    @PostMapping("/items/{id}/reserve")
    public ResponseEntity<?> reserve(@PathVariable Long id, @RequestParam int qty) {
        if (qty <= 0) return ResponseEntity.badRequest().body("qty debe ser > 0");
        int updated = repo.reserveStock(id, qty);
        if (updated == 1) return ResponseEntity.noContent().build();
        return ResponseEntity.badRequest().body("Item no existe o stock insuficiente");
    }

    // ✅ POST liberar stock: /catalog/items/{id}/release?qty=2
    @PostMapping("/items/{id}/release")
    public ResponseEntity<?> release(@PathVariable Long id, @RequestParam int qty) {
        if (qty <= 0) return ResponseEntity.badRequest().body("qty debe ser > 0");
        int updated = repo.releaseStock(id, qty);
        if (updated == 1) return ResponseEntity.noContent().build();
        return ResponseEntity.badRequest().body("Item no existe");
    }
}
