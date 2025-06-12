package com.rohlik.case_study.controller;

import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class OrderControllerResponse503Test {

    private final OrderService orderService = null; // Not needed for fallback tests
    private final OrderController controller = new OrderController(orderService);

    @Test
    void createOrderFallback_shouldReturn503() {
        CreateOrderDto dto = new CreateOrderDto();
        Throwable t = new RuntimeException("Simulated failure");
        ResponseEntity<Order> response = controller.createOrderFallback(dto, t);
        assertEquals(503, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void cancelOrderFallback_shouldReturn503() {
        Throwable t = new RuntimeException("Simulated failure");
        ResponseEntity<Void> response = controller.cancelOrderFallback(1L, t);
        assertEquals(503, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void payOrderFallback_shouldReturn503() {
        Throwable t = new RuntimeException("Simulated failure");
        ResponseEntity<Void> response = controller.payOrderFallback(1L, t);
        assertEquals(503, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}