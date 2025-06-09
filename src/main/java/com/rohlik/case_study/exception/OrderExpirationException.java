package com.rohlik.case_study.exception;

public class OrderExpirationException extends RuntimeException {
    public OrderExpirationException(String message) {
        super(message);
    }
}