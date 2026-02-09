package com.ucshop.order.web;

import com.ucshop.order.domain.OrderEntity;

import java.math.BigDecimal;

import java.time.OffsetDateTime;

public class OrderResponse {

    private Long id;
    private Long itemId;
    private Integer quantity;
    private OffsetDateTime createdAt;

    // enriquecimiento (solo GET)
    private String itemName;
    private BigDecimal itemPrice;

    public static OrderResponse from(OrderEntity o) {
        OrderResponse r = new OrderResponse();
        r.id = o.getId();
        r.itemId = o.getItemId();
        r.quantity = o.getQuantity();
        r.createdAt = o.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public Integer getQuantity() { return quantity; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public String getItemName() { return itemName; }
    public BigDecimal getItemPrice() { return itemPrice; }


    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setItemPrice(BigDecimal itemPrice) { this.itemPrice = itemPrice; }
}
