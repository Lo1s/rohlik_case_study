package com.rohlik.case_study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.dto.CreateOrderDto.OrderItemDto;
import com.rohlik.case_study.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerCircuitBreakerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void createOrder_shouldReturn503_whenCircuitBreakerOpens() throws Exception {
        // Simulate service failure
        doThrow(new RuntimeException("Simulated service failure"))
                .when(orderService).createOrder(any(CreateOrderDto.class));

        CreateOrderDto dto = new CreateOrderDto();
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(1);
        dto.setItems(List.of(itemDto));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isServiceUnavailable()); // 503
    }

    @Test
    void cancelOrder_shouldReturn503_whenCircuitBreakerOpens() throws Exception {
        doThrow(new RuntimeException("Simulated service failure"))
                .when(orderService).cancelOrder(1L);

        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isServiceUnavailable()); // 503
    }

    @Test
    void payOrder_shouldReturn503_whenCircuitBreakerOpens() throws Exception {
        doThrow(new RuntimeException("Simulated service failure"))
                .when(orderService).payOrder(1L);

        mockMvc.perform(post("/api/orders/1/pay"))
                .andExpect(status().isServiceUnavailable()); // 503
    }
}