package com.rohlik.case_study.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.dto.CreateOrderDto.OrderItemDto;
import com.rohlik.case_study.entity.Product;
import com.rohlik.case_study.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createOrder_shouldReturn400_onInvalidInput() throws Exception {
        CreateOrderDto dto = new CreateOrderDto(); // No items

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_shouldReturn200_onValidInput() throws Exception {
        // Ensure a product exists
        Product product = new Product();
        product.setName("Test Product");
        product.setQuantity(10);
        product.setPrice(100.0);
        product = productRepository.save(product);

        CreateOrderDto dto = new CreateOrderDto();
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(product.getId());
        itemDto.setQuantity(1);
        dto.setItems(List.of(itemDto));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrder_shouldReturn204() throws Exception {
        // Create a product and order first
        Product product = new Product();
        product.setName("Cancel Test Product");
        product.setQuantity(10);
        product.setPrice(50.0);
        product = productRepository.save(product);

        CreateOrderDto dto = new CreateOrderDto();
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(product.getId());
        itemDto.setQuantity(1);
        dto.setItems(List.of(itemDto));

        String orderResponse = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("Order response: " + orderResponse);

        JsonNode orderJson = objectMapper.readTree(orderResponse);
        Long orderId = orderJson.get("id").asLong();

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isNoContent());
    }

    @Test
    void payOrder_shouldReturn204() throws Exception {
        // Create a product and order first
        Product product = new Product();
        product.setName("Pay Test Product");
        product.setQuantity(10);
        product.setPrice(75.0);
        product = productRepository.save(product);

        CreateOrderDto dto = new CreateOrderDto();
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(product.getId());
        itemDto.setQuantity(1);
        dto.setItems(List.of(itemDto));

        String orderResponse = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode orderJson = objectMapper.readTree(orderResponse);
        Long orderId = orderJson.get("id").asLong();

        mockMvc.perform(post("/api/orders/" + orderId + "/pay"))
                .andExpect(status().isNoContent());
    }
}