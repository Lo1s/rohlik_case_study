package com.rohlik.case_study.controller;

import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody CreateOrderDto dto) {
        Order order = orderService.createOrder(dto);
        return ResponseEntity.ok(order);
    }

    /** Cancel an order (releases reserved stock) **/
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    /** Pay for an order (marks order as paid) **/
    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> payOrder(@PathVariable Long id) {
        orderService.payOrder(id);
        return ResponseEntity.noContent().build();
    }
}