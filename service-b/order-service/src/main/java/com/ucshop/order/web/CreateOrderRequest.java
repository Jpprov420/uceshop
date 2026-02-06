package com.ucshop.order.web;

public class CreateOrderRequest {
    private Long itemId;
    private Integer quantity;

    public Long getItemId() { return itemId; }
    public Integer getQuantity() { return quantity; }

    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
