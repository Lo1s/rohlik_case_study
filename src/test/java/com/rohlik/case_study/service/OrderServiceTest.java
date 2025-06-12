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
import com.rohlik.case_study.service.OrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @InjectMocks private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_success() {
        // Arrange
        Product product = new Product();
        product.setQuantity(10);

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        CreateOrderDto dto = new CreateOrderDto();
        dto.setItems(List.of(itemDto));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Order order = orderService.createOrder(dto);

        // Assert
        assertNotNull(order);
        verify(productRepository).saveAll(anySet());
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void createOrder_outOfStock() {
        Product product = new Product();
        product.setQuantity(1);

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        CreateOrderDto dto = new CreateOrderDto();
        dto.setItems(List.of(itemDto));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(OutOfStockException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_productNotFound() {
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(99L);
        itemDto.setQuantity(1);

        CreateOrderDto dto = new CreateOrderDto();
        dto.setItems(List.of(itemDto));

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void cancelOrder_success() {
        Order order = mock(Order.class);
        when(order.getCanceled()).thenReturn(false);
        when(order.getPaid()).thenReturn(false);
        when(order.getItems()).thenReturn(List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        verify(order).setCanceled(true);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_alreadyPaid() {
        Order order = mock(Order.class);
        when(order.getCanceled()).thenReturn(false);
        when(order.getPaid()).thenReturn(true);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void payOrder_success() {
        Order order = mock(Order.class);
        when(order.getCanceled()).thenReturn(false);
        when(order.getPaid()).thenReturn(false);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.payOrder(1L);

        verify(order).setPaid(true);
        verify(orderRepository).save(order);
    }


    @Test
    void payOrder_alreadyPaid() {
        Order order = mock(Order.class);
        when(order.getCanceled()).thenReturn(false);
        when(order.getPaid()).thenReturn(true);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.payOrder(1L));
    }

    @Test
    void payOrder_canceled() {
        Order order = mock(Order.class);
        when(order.getCanceled()).thenReturn(true);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.payOrder(1L));
    }

    @Test
    void expireOrders_shouldCancelUnpaidOrdersAndReleaseStock() {
        Product product = new Product();
        product.setQuantity(5);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        Order order = mock(Order.class);
        when(order.getPaid()).thenReturn(false);
        when(order.getCanceled()).thenReturn(false);
        when(order.getItems()).thenReturn(List.of(item));

        List<Order> expiredOrders = List.of(order);

        when(orderRepository.findExpiredOrders(any())).thenReturn(expiredOrders);

        orderService.expireOrders();

        verify(order).setCanceled(true);
        verify(orderRepository).save(order);
        verify(productRepository).save(product);
    }
}