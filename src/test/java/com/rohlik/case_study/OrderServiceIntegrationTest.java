package com.rohlik.case_study;

import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.dto.CreateOrderDto.OrderItemDto;
import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.entity.Product;
import com.rohlik.case_study.service.OrderService;
import com.rohlik.case_study.repository.OrderRepository;
import com.rohlik.case_study.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceIntegrationTest {

    @Autowired private OrderService orderService;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;

    @Test
    void createOrder_andPayOrder() {
        Product product = new Product();
        product.setQuantity(10);
        product.setName("Test Product");
        product.setPrice(100.0);
        product = productRepository.save(product);

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(product.getId());
        itemDto.setQuantity(2);

        CreateOrderDto dto = new CreateOrderDto();
        dto.setItems(List.of(itemDto));

        Order order = orderService.createOrder(dto);
        assertNotNull(order.getId());
        assertFalse(order.getPaid());

        orderService.payOrder(order.getId());
        Order paidOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertTrue(paidOrder.getPaid());
    }
}