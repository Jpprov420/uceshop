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

    // ✅ GET: lista órdenes enriquecidas con info del item (nombre/precio)
    @GetMapping("/orders")
    public List<OrderResponse> all() {
        return repo.findAll().stream().map(o -> {
            OrderResponse r = OrderResponse.from(o);

            catalogClient.getItemById(o.getItemId()).ifPresent(info -> {
                r.setItemName(info.getName());
                r.setItemPrice(info.getPrice());
            });

            return r;
        }).toList();
    }

    // ✅ GET: ver una orden por ID (enriquecida con nombre/precio)
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> byId(@PathVariable Long id) {

        return repo.findById(id)
                .map(o -> {
                    OrderResponse r = OrderResponse.from(o);

                    catalogClient.getItemById(o.getItemId()).ifPresent(info -> {
                        r.setItemName(info.getName());
                        r.setItemPrice(info.getPrice());
                    });

                    return ResponseEntity.ok(r);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ POST: mismo body {itemId, quantity}, pero ahora descuenta stock
    @PostMapping("/orders")
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest req) {

        if (req.getItemId() == null || req.getQuantity() == null || req.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("itemId y quantity son obligatorios; quantity>0");
        }

        // 1) Reservar stock
        boolean reserved = catalogClient.reserveStock(req.getItemId(), req.getQuantity());
        if (!reserved) {
            return ResponseEntity.badRequest().body("No se pudo reservar stock (item inexistente o stock insuficiente)");
        }

        // 2) Guardar orden
        OrderEntity o = new OrderEntity();
        o.setItemId(req.getItemId());
        o.setQuantity(req.getQuantity());

        OrderEntity saved = repo.save(o);
        return ResponseEntity.ok(saved);
    }
}
