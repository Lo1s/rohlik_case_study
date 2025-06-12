package com.rohlik.case_study.dto;

public class ParsedOrderItemDto {
    private String productName;
    private Integer quantity;
    private Long productId;
    
    public ParsedOrderItemDto() {}
    
    public ParsedOrderItemDto(String productName, Integer quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }
    
    public ParsedOrderItemDto(String productName, Integer quantity, Long productId) {
        this.productName = productName;
        this.quantity = quantity;
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
