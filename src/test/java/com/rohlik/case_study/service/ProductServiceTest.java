package com.rohlik.case_study.service;

import com.rohlik.case_study.dto.CreateProductDto;
import com.rohlik.case_study.dto.ProductDto;
import com.rohlik.case_study.dto.UpdateProductDto;
import com.rohlik.case_study.entity.Product;
import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.entity.OrderItem;
import com.rohlik.case_study.exception.ProductInActiveOrderException;
import com.rohlik.case_study.exception.ResourceNotFoundException;
import com.rohlik.case_study.repository.ProductRepository;
import com.rohlik.case_study.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @InjectMocks private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProduct_success() {
        CreateProductDto dto = new CreateProductDto();
        dto.setName("Test Product");
        dto.setQuantity(10);
        dto.setPrice(100.0);

        Product saved = new Product("Test Product", 10, 100.0);

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDto result = productService.createProduct(dto);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(10, result.getQuantity());
        assertEquals(100.0, result.getPrice());
    }

    @Test
    void updateProduct_success() {
        Product product = new Product("Old", 1, 1.0);

        UpdateProductDto dto = new UpdateProductDto();
        dto.setName("Updated");
        dto.setQuantity(5);
        dto.setPrice(50.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto result = productService.updateProduct(1L, dto);

        assertEquals("Updated", result.getName());
        assertEquals(5, result.getQuantity());
        assertEquals(50.0, result.getPrice());
    }

    @Test
    void updateProduct_notFound() {
        UpdateProductDto dto = new UpdateProductDto();
        dto.setName("Updated");
        dto.setQuantity(5);
        dto.setPrice(50.0);

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(1L, dto));
    }

    @Test
    void listProducts_success() {
        Product p1 = new Product("P1", 1, 1.0);
        Product p2 = new Product("P2", 2, 2.0);

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        List<ProductDto> products = productService.listProducts();
        assertEquals(2, products.size());
        assertEquals("P1", products.get(0).getName());
        assertEquals("P2", products.get(1).getName());
    }

    @Test
    void deleteProduct_success_whenNoActiveOrders() {
        Product product = new Product("ToDelete", 1, 1.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.findAll()).thenReturn(List.of());

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_notFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(1L));
    }
}