package com.ucshop.order.web;

import com.ucshop.order.domain.OrderEntity;
import com.ucshop.order.integration.CatalogClient;
import com.ucshop.order.repo.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    private final OrderRepository repo;
    private final CatalogClient catalogClient;

    public OrderController(OrderRepository repo, CatalogClient catalogClient) {
        this.repo = repo;
        this.catalogClient = catalogClient;
    }

    // ✅ GET: listar todas las órdenes
    @GetMapping("/orders")
    public ResponseEntity<List<OrderEntity>> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    // ✅ (opcional) GET: obtener una orden por id
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Orden no encontrada: " + id));
    }

    // ✅ POST: crear orden
    @PostMapping("/orders")
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest req) {

        if (req.getItemId() == null || req.getQuantity() == null || req.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("itemId y quantity son obligatorios; quantity>0");
        }

        boolean exists = catalogClient.itemExists(req.getItemId());
        if (!exists) {
            return ResponseEntity.badRequest().body("itemId no existe en catalog");
        }

        OrderEntity o = new OrderEntity();
        o.setItemId(req.getItemId());
        o.setQuantity(req.getQuantity());

        OrderEntity saved = repo.save(o);
        return ResponseEntity.ok(saved);
    }
}
