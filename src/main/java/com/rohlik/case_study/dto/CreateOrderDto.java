package com.rohlik.case_study.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateOrderDto {
    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemDto> items;

    // Getters and setters
    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    public static class OrderItemDto {
        @NotNull
        private Long productId;

        @NotNull @Min(1)
        private Integer quantity;

        // Getters and setters

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
