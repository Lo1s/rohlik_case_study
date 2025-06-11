package com.rohlik.case_study.controller;

import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/api/orders")
@Validated
/**
 * OrderController handles order-related operations such as creating, canceling, and paying for orders.
 * It uses the OrderService to perform business logic and interacts with the Order entity.
 */
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Create an order (deducts stock) **/
    @PostMapping
    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody CreateOrderDto dto) {
        Order order = orderService.createOrder(dto);
        return ResponseEntity.ok(order);
    }

    /** Cancel an order (releases reserved stock) **/
    @PostMapping("/{id}/cancel")
    @CircuitBreaker(name = "orderService", fallbackMethod = "cancelOrderFallback")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    /** Pay for an order (marks order as paid) **/
    @PostMapping("/{id}/pay")
    @CircuitBreaker(name = "orderService", fallbackMethod = "payOrderFallback")
    public ResponseEntity<Void> payOrder(@PathVariable Long id) {
        orderService.payOrder(id);
        return ResponseEntity.noContent().build();
    }

    // --- Fallback methods ---

    public ResponseEntity<Order> createOrderFallback(CreateOrderDto dto, Throwable t) {
        return ResponseEntity.status(503).build();
    }

    public ResponseEntity<Void> cancelOrderFallback(Long id, Throwable t) {
        return ResponseEntity.status(503).build();
    }

    public ResponseEntity<Void> payOrderFallback(Long id, Throwable t) {
        return ResponseEntity.status(503).build();
    }
}