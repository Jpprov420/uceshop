package com.uceshop.catalog.api;

import java.math.BigDecimal;

public class CreateItemRequest {
    private String name;
    private BigDecimal price;
    private Integer quantity;

    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantity() { return quantity; }

    public void setName(String name) { this.name = name; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
