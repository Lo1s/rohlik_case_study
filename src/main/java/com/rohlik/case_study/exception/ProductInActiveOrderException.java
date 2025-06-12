package com.rohlik.case_study.exception;

public class ProductInActiveOrderException extends RuntimeException {
    public ProductInActiveOrderException(Long productId) {
        super("Cannot delete product with active orders: " + productId);
    }
}