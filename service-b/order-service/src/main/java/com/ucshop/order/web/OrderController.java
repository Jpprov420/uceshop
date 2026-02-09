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

    // ✅ GET por ID: /orders/{id}
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> byId(@PathVariable Long id) {
        return repo.findById(id)
                .<ResponseEntity<?>>map(o -> {
                    OrderResponse r = OrderResponse.from(o);
                    catalogClient.getItemById(o.getItemId()).ifPresent(info -> {
                        r.setItemName(info.getName());
                        r.setItemPrice(info.getPrice());
                    });
                    return ResponseEntity.ok(r);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ POST: mismo body {itemId, quantity}, descuenta stock
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

    // ✅ PUT: actualizar orden (ajusta stock con delta)
    @PutMapping("/orders/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CreateOrderRequest req) {

        if (req.getItemId() == null || req.getQuantity() == null || req.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("itemId y quantity son obligatorios; quantity>0");
        }

        return repo.findById(id).map(existing -> {

            Long oldItemId = existing.getItemId();
            int oldQty = existing.getQuantity();

            Long newItemId = req.getItemId();
            int newQty = req.getQuantity();

            // Caso 1: mismo item -> ajustar diferencia
            if (oldItemId.equals(newItemId)) {
                int diff = newQty - oldQty;

                if (diff > 0) {
                    // necesito reservar extra
                    boolean ok = catalogClient.reserveStock(newItemId, diff);
                    if (!ok) return ResponseEntity.badRequest().body("Stock insuficiente para aumentar la orden");
                } else if (diff < 0) {
                    // libero excedente
                    catalogClient.releaseStock(newItemId, Math.abs(diff));
                }

            } else {
                // Caso 2: cambió item -> libero todo del viejo, reservo todo del nuevo
                catalogClient.releaseStock(oldItemId, oldQty);
                boolean ok = catalogClient.reserveStock(newItemId, newQty);
                if (!ok) {
                    // rollback manual simple: vuelvo a reservar lo del item viejo para no dejar inconsistente
                    catalogClient.reserveStock(oldItemId, oldQty);
                    return ResponseEntity.badRequest().body("Stock insuficiente para el nuevo item; no se aplicó el cambio");
                }
            }

            existing.setItemId(newItemId);
            existing.setQuantity(newQty);

            OrderEntity saved = repo.save(existing);
            return ResponseEntity.ok(saved);

        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ DELETE: elimina orden y libera stock
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repo.findById(id).map(existing -> {
            catalogClient.releaseStock(existing.getItemId(), existing.getQuantity());
            repo.delete(existing);
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
