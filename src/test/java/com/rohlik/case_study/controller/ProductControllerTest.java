package com.rohlik.case_study.controller;

import com.rohlik.case_study.dto.CreateProductDto;
import com.rohlik.case_study.dto.UpdateProductDto;
import com.rohlik.case_study.dto.ProductDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductControllerTest {

    private final ProductController controller = new ProductController(null);

    @Test
    void listProductsFallback_shouldReturn503() {
        ResponseEntity<List<ProductDto>> response = controller.listProductsFallback(new RuntimeException("fail"));
        assertEquals(503, response.getStatusCodeValue());
    }

    @Test
    void createProductFallback_shouldReturn503() {
        ResponseEntity<?> response = controller.createProductFallback(new CreateProductDto(), new RuntimeException("fail"));
        assertEquals(503, response.getStatusCodeValue());
    }

    @Test
    void updateProductFallback_shouldReturn503() {
        ResponseEntity<?> response = controller.updateProductFallback(1L, new UpdateProductDto(), new RuntimeException("fail"));
        assertEquals(503, response.getStatusCodeValue());
    }

    @Test
    void deleteProductFallback_shouldReturn503() {
        ResponseEntity<?> response = controller.deleteProductFallback(1L, new RuntimeException("fail"));
        assertEquals(503, response.getStatusCodeValue());
    }
}