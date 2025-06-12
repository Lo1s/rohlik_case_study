package com.rohlik.case_study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohlik.case_study.dto.CreateProductDto;
import com.rohlik.case_study.dto.UpdateProductDto;
import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.dto.CreateOrderDto.OrderItemDto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProduct_shouldReturn400_onInvalidInput() throws Exception {
        CreateProductDto dto = new CreateProductDto(); // Missing required fields

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAndListProduct_shouldReturn200_andContainProduct() throws Exception {
        CreateProductDto dto = new CreateProductDto();
        dto.setName("Test Product");
        dto.setQuantity(10);
        dto.setPrice(99.99);

        // Create product
        String response = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andReturn().getResponse().getContentAsString();

        // List products and check the created product is present
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Test Product")));
    }

    @Test
    void updateProduct_shouldReturn200_andUpdatedProduct() throws Exception {
        // First, create a product
        CreateProductDto createDto = new CreateProductDto();
        createDto.setName("ToUpdate");
        createDto.setQuantity(5);
        createDto.setPrice(10.0);

        String createResponse = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract the product ID from the response
        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        // Prepare update DTO
        UpdateProductDto updateDto = new UpdateProductDto();
        updateDto.setName("UpdatedName");
        updateDto.setQuantity(20);
        updateDto.setPrice(20.0);

        // Update product
        mockMvc.perform(put("/api/products/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"))
                .andExpect(jsonPath("$.quantity").value(20))
                .andExpect(jsonPath("$.price").value(20.0));
    }

    @Test
    void deleteProduct_shouldReturn204() throws Exception {
        // First, create a product
        CreateProductDto createDto = new CreateProductDto();
        createDto.setName("ToDelete");
        createDto.setQuantity(1);
        createDto.setPrice(1.0);

        String createResponse = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        // Delete product
        mockMvc.perform(delete("/api/products/" + id))
                .andExpect(status().isNoContent());

        // Ensure product is not listed anymore
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(id.intValue()))));
    }

    @Test
    void deleteProduct_shouldReturn204_whenNoOrdersDependOnIt() throws Exception {
        // Create a product
        CreateProductDto createDto = new CreateProductDto();
        createDto.setName("IndependentProduct");
        createDto.setQuantity(10);
        createDto.setPrice(5.0);

        String createResponse = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        // Delete product (should succeed since no orders depend on it)
        mockMvc.perform(delete("/api/products/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_shouldReturn4xx_whenOrdersDependOnIt() throws Exception {
        // Create a product
        CreateProductDto createDto = new CreateProductDto();
        createDto.setName("DependentProduct");
        createDto.setQuantity(10);
        createDto.setPrice(5.0);

        String createResponse = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long productId = objectMapper.readTree(createResponse).get("id").asLong();

        // Create an order that uses this product
        CreateOrderDto orderDto = new CreateOrderDto();
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(productId);
        itemDto.setQuantity(1);
        orderDto.setItems(List.of(itemDto));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk());

        // Try to delete the product (should fail with 4xx)
        mockMvc.perform(delete("/api/products/" + productId))
                .andExpect(status().is4xxClientError());
    }
}