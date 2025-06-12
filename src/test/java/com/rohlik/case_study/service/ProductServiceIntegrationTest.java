package com.rohlik.case_study.service;

import com.rohlik.case_study.dto.CreateProductDto;
import com.rohlik.case_study.dto.ProductDto;
import com.rohlik.case_study.dto.UpdateProductDto;
import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.entity.OrderItem;
import com.rohlik.case_study.entity.Product;
import com.rohlik.case_study.exception.ProductInActiveOrderException;
import com.rohlik.case_study.repository.OrderRepository;
import com.rohlik.case_study.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void createProduct_shouldPersistAndReturnProductDto() {
        CreateProductDto dto = new CreateProductDto();
        dto.setName("New Product");
        dto.setQuantity(5);
        dto.setPrice(10.0);

        ProductDto result = productService.createProduct(dto);

        assertNotNull(result.getId());
        assertEquals("New Product", result.getName());
        assertEquals(5, result.getQuantity());
        assertEquals(10.0, result.getPrice());
        assertTrue(productRepository.existsById(result.getId()));
    }

    @Test
    void updateProduct_shouldUpdateFields() {
        Product product = new Product("Old Name", 3, 5.0);
        product = productRepository.save(product);

        UpdateProductDto dto = new UpdateProductDto();
        dto.setName("Updated Name");
        dto.setQuantity(10);
        dto.setPrice(20.0);

        ProductDto updated = productService.updateProduct(product.getId(), dto);

        assertEquals("Updated Name", updated.getName());
        assertEquals(10, updated.getQuantity());
        assertEquals(20.0, updated.getPrice());
    }

    @Test
    void listProducts_shouldReturnAllProducts() {
        productRepository.save(new Product("P1", 1, 1.0));
        productRepository.save(new Product("P2", 2, 2.0));

        List<ProductDto> products = productService.listProducts();
        assertThat(products).extracting(ProductDto::getName).contains("P1", "P2");
    }

    @Test
    void deleteProduct_shouldRemoveProduct_whenNoActiveOrders() {
        Product product = new Product("ToDelete", 1, 1.0);
        product = productRepository.save(product);

        productService.deleteProduct(product.getId());

        assertFalse(productRepository.existsById(product.getId()));
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductInActiveOrder() {
        // Setup: create product
        Product product = new Product("InOrder", 5, 5.0);
        productRepository.save(product);

        // Create an active order referencing this product
        Order order = new Order();
        order.setPaid(false);
        order.setCanceled(false);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setOrder(order);

        order.setItems(List.of(item));

        // Save order and item (assuming cascade is set up, otherwise save item separately)
        orderRepository.save(order);

        // This should throw ProductInActiveOrderException
        assertThrows(ProductInActiveOrderException.class, () -> {
            productService.deleteProduct(product.getId());
        });
    }
}