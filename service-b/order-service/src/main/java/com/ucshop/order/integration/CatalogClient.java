package com.ucshop.order.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Component
public class CatalogClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CatalogClient(
            RestTemplate restTemplate,
            @Value("${catalog.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * POST /catalog/items/{id}/reserve?qty=X
     * Devuelve true si reservó stock, false si no (stock insuficiente o item no existe).
     */
    public boolean reserveStock(Long itemId, int qty) {

        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/catalog/items/{id}/reserve")
                .queryParam("qty", qty)
                .buildAndExpand(itemId)
                .toUriString();

        try {
            restTemplate.postForEntity(url, null, Void.class);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean releaseStock(Long itemId, int qty) {
    String url = UriComponentsBuilder
            .fromUriString(baseUrl + "/catalog/items/{id}/release")
            .queryParam("qty", qty)
            .buildAndExpand(itemId)
            .toUriString();

    try {
        restTemplate.postForEntity(url, null, Void.class);
        return true;
    } catch (Exception ex) {
        return false;
    }
}

    /**
     * GET /catalog/items/{id}
     * Sirve para enriquecer el GET /orders (mostrar nombre/precio).
     */
public Optional<ItemInfo> getItemById(Long itemId) {
    String url = baseUrl + "/catalog/items/" + itemId;

    try {
        ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> body = resp.getBody();
        if (body == null) return Optional.empty();

        ItemInfo info = new ItemInfo();
        info.setId(((Number) body.get("id")).longValue());
        info.setName((String) body.get("name"));
        info.setPrice(new BigDecimal(String.valueOf(body.get("price"))));
        info.setQuantity(((Number) body.get("quantity")).intValue());

        return Optional.of(info);
    } catch (Exception ex) {
        return Optional.empty();
    }
}

    // DTO interno simple
    public static class ItemInfo {
        private Long id;
        private String name;
        private BigDecimal price;
        private int quantity;

        public Long getId() { return id; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
        public int getQuantity() { return quantity; }

        public void setId(Long id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
