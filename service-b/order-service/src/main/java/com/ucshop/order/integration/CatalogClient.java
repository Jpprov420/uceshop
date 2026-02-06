package com.ucshop.order.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class CatalogClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CatalogClient(
            RestTemplate restTemplate,
            @Value("${catalog.base-url:http://catalog-service:8080}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public boolean itemExists(Long itemId) {
        String url = baseUrl + "/catalog/items";
        System.out.println("CATALOG GET => " + url);

        ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> items = resp.getBody();
        if (items == null) return false;

        boolean exists = items.stream().anyMatch(m -> {
            Object idObj = m.get("id");
            if (!(idObj instanceof Number)) return false;
            long id = ((Number) idObj).longValue();
            return id == itemId;
        });

        System.out.println("CATALOG itemExists(" + itemId + ") => " + exists);
        return exists;
    }
}
