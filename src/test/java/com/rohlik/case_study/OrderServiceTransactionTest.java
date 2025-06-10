package com.rohlik.case_study;

import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.dto.CreateOrderDto.OrderItemDto;
import com.rohlik.case_study.service.OrderService;
import com.rohlik.case_study.entity.Product;
import com.rohlik.case_study.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceTransactionTest {

    @Autowired private OrderService orderService;
    @Autowired private ProductRepository productRepository;

    @Test
    void createOrder_shouldRollbackOnOutOfStock() {
        Product product = new Product();
        product.setQuantity(1);
        product.setName("Test Product");
        product.setPrice(100.0);
        product = productRepository.save(product);

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(product.getId());
        itemDto.setQuantity(2);

        CreateOrderDto dto = new CreateOrderDto();
        dto.setItems(List.of(itemDto));

        assertThrows(Exception.class, () -> orderService.createOrder(dto));
        Product refreshed = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(1, refreshed.getQuantity()); // Should not be decremented
    }
}