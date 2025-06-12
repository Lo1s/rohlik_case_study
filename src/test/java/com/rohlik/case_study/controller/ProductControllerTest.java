package com.rohlik.case_study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohlik.case_study.dto.CreateProductDto;
import com.rohlik.case_study.dto.ProductDto;
import com.rohlik.case_study.dto.UpdateProductDto;
import com.rohlik.case_study.entity.Product;
import com.rohlik.case_study.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void createProduct_success() throws Exception {
        CreateProductDto dto = new CreateProductDto();
        dto.setName("Test Product");
        dto.setQuantity(10);
        dto.setPrice(100.0);

        when(productService.createProduct(any(CreateProductDto.class)))
                .thenReturn(new com.rohlik.case_study.dto.ProductDto(1L, "Test Product", 10, 100.0));

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.price").value(100.0));
    }

    @Test
    void updateProduct_success() throws Exception {
        UpdateProductDto dto = new UpdateProductDto();
        dto.setName("Updated");
        dto.setQuantity(5);
        dto.setPrice(50.0);

        when(productService.updateProduct(eq(1L), any(UpdateProductDto.class)))
                .thenReturn(new com.rohlik.case_study.dto.ProductDto(1L, "Updated", 5, 50.0));

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.price").value(50.0));
    }

    @Test
    void listProducts_success() throws Exception {
        when(productService.listProducts()).thenReturn(List.of(
                new com.rohlik.case_study.dto.ProductDto(1L, "P1", 1, 1.0),
                new com.rohlik.case_study.dto.ProductDto(2L, "P2", 2, 2.0)
        ));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("P1"))
                .andExpect(jsonPath("$[1].name").value("P2"));
    }

    @Test
    void deleteProduct_success() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void listProductsFallback_shouldReturn503() {
        ResponseEntity<List<ProductDto>> response = productController.listProductsFallback(new RuntimeException("fail"));
        assertEquals(503, response.getStatusCodeValue());
    }

    @Test
    void createProductFallback_shouldReturn503() {
        ResponseEntity<?> response = productController.createProductFallback(new CreateProductDto(), new RuntimeException("fail"));
        assertEquals(503, response.getStatusCodeValue());
    }

    @Test
    void updateProductFallback_shouldReturn503() {
        ResponseEntity<?> response = productController.updateProductFallback(1L, new UpdateProductDto(), new RuntimeException("fail"));
        assertEquals(503, response.getStatusCodeValue());
    }

    @Test
    void deleteProductFallback_shouldReturn503() {
        ResponseEntity<?> response = productController.deleteProductFallback(1L, new RuntimeException("fail"));
        assertEquals(503, response.getStatusCodeValue());
    }
}