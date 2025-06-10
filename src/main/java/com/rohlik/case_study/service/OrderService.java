package com.rohlik.case_study.service;

import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.dto.CreateOrderDto.OrderItemDto;
import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.entity.OrderItem;
import com.rohlik.case_study.entity.Product;
import com.rohlik.case_study.exception.OutOfStockException;
import com.rohlik.case_study.exception.ResourceNotFoundException;
import com.rohlik.case_study.repository.OrderItemRepository;
import com.rohlik.case_study.repository.OrderRepository;
import com.rohlik.case_study.repository.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    // Orders older than this (in minutes) will be expired (if unpaid)
    private final long RESERVATION_MINUTES = 30;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderDto dto) {
        // 1) Check stock availability for each requested item
        Map<Product, Integer> toDeduct = new HashMap<>();
        List<String> insufficient = new ArrayList<>();

        for (OrderItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemDto.getProductId()));
            if (product.getQuantity() < itemDto.getQuantity()) {
                insufficient.add("Product ID " + product.getId() + " has insufficient stock");
            } else {
                toDeduct.put(product, itemDto.getQuantity());
            }
        }
        if (!insufficient.isEmpty()) {
            throw new OutOfStockException(String.join("; ", insufficient));
        }

        // 2) Deduct stock immediately (reservation)
        toDeduct.forEach((product, qty) ->
                product.setQuantity(product.getQuantity() - qty)
        );
        productRepository.saveAll(toDeduct.keySet());

        // 3) Create and save the new Order
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setPaid(false);
        order.setCanceled(false);
        order = orderRepository.save(order);

        // 4) Create and save OrderItems
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId()).get();
            OrderItem orderItem = new OrderItem(order, product, itemDto.getQuantity());
            orderItem = orderItemRepository.save(orderItem);
            items.add(orderItem);
        }
        order.setItems(items);

        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getCanceled()) {
            throw new IllegalStateException("Order already canceled: " + orderId);
        }

        if (order.getPaid()) {
            throw new IllegalStateException("Cannot cancel a paid order: " + orderId);
        }

        // Mark order as canceled
        order.setCanceled(true);

        // Release reserved stock for each item
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }
        orderRepository.save(order);
    }

    @Transactional
    public void payOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getCanceled()) {
            throw new IllegalStateException("Cannot pay a canceled order: " + orderId);
        }
        if (order.getPaid()) {
            throw new IllegalStateException("Order already paid: " + orderId);
        }

        order.setPaid(true);
        orderRepository.save(order);
    }

    /**
     * Scheduled job that runs every 10 minutes (600,000 ms). It finds all orders
     * older than 30 minutes that are still unpaid/not canceled, cancels them,
     * and releases their stock back to products.
     */
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void expireOrders() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(RESERVATION_MINUTES);
        List<Order> expired = orderRepository.findExpiredOrders(expirationTime);

        for (Order order : expired) {
            if (!order.getPaid() && !order.getCanceled()) {
                // Release stock
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
                // Mark the order as canceled
                order.setCanceled(true);
                orderRepository.save(order);
            }
        }
    }
}
