package com.uceshop.catalog.api;

import com.uceshop.catalog.repo.ItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalog")
public class CatalogReservationController {

    private final ItemRepository repo;

    public CatalogReservationController(ItemRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> reserve(@RequestBody ReserveRequest req) {

        if (req.itemId() == null || req.quantity() == null || req.quantity() <= 0) {
            return ResponseEntity.badRequest().body("itemId y quantity son obligatorios; quantity>0");
        }

        int updated = repo.reserveStock(req.itemId(), req.quantity());
        if (updated == 1) {
            return ResponseEntity.ok("RESERVED");
        }
        return ResponseEntity.status(409).body("Sin stock");
    }

    public record ReserveRequest(Long itemId, Integer quantity) {}
}
