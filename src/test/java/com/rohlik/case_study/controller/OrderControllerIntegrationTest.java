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
    void createOrder_withMultipleProducts_shouldReturn200() throws Exception {
        // Create two products
        Product product1 = new Product();
        product1.setName("Banana");
        product1.setQuantity(20);
        product1.setPrice(10.0);
        product1 = productRepository.save(product1);

        Product product2 = new Product();
        product2.setName("Apple");
        product2.setQuantity(30);
        product2.setPrice(15.0);
        product2 = productRepository.save(product2);

        // Prepare order DTO with both products
        CreateOrderDto dto = new CreateOrderDto();
        OrderItemDto item1 = new OrderItemDto();
        item1.setProductId(product1.getId());
        item1.setQuantity(5);
        OrderItemDto item2 = new OrderItemDto();
        item2.setProductId(product2.getId());
        item2.setQuantity(4);
        dto.setItems(List.of(item1, item2));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void createOrder_withMaxAvailableQuantity_shouldSucceed_and_ExceedingQuantityShouldFail() throws Exception {
        // Create a product with limited stock
        Product product = new Product();
        product.setName("Limited Stock Product");
        product.setQuantity(7);
        product.setPrice(20.0);
        product = productRepository.save(product);

        // 1. Create order with max available quantity
        CreateOrderDto maxOrderDto = new CreateOrderDto();
        OrderItemDto maxItem = new OrderItemDto();
        maxItem.setProductId(product.getId());
        maxItem.setQuantity(7); // exactly available
        maxOrderDto.setItems(List.of(maxItem));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxOrderDto)))
                .andExpect(status().isOk());

        // 2. Try to create another order with quantity exceeding available (should fail)
        CreateOrderDto exceedingOrderDto = new CreateOrderDto();
        OrderItemDto exceedingItem = new OrderItemDto();
        exceedingItem.setProductId(product.getId());
        exceedingItem.setQuantity(1); // no stock left
        exceedingOrderDto.setItems(List.of(exceedingItem));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exceedingOrderDto)))
                .andExpect(status().isBadRequest());
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
    void cancelOrder_shouldReleaseReservedStock() throws Exception {
        // Create a product with limited stock
        Product product = new Product();
        product.setName("Stock Release Product");
        product.setQuantity(5);
        product.setPrice(30.0);
        product = productRepository.save(product);              
        // Create an order that reserves all stock
        CreateOrderDto dto = new CreateOrderDto();
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(product.getId());
        itemDto.setQuantity(5);
        dto.setItems(List.of(itemDto)); 

        String orderResponse = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();       

        JsonNode orderJson = objectMapper.readTree(orderResponse);
        Long orderId = orderJson.get("id").asLong();            
        // Cancel the order
        mockMvc.perform(post("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isNoContent());             
        // Check that the product stock is restored
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updatedProduct.getQuantity()).isEqualTo(5);
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