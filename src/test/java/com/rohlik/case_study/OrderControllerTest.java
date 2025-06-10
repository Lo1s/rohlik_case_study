package com.rohlik.case_study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohlik.case_study.controller.OrderController;
import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.dto.CreateOrderDto.OrderItemDto;
import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private OrderService orderService;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void createOrder_shouldReturnCreatedOrder() throws Exception {
        CreateOrderDto dto = new CreateOrderDto();
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        dto.setItems(List.of(itemDto));

        Order order = new Order();

        Mockito.when(orderService.createOrder(any(CreateOrderDto.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void createOrder_shouldReturnBadRequest_onInvalidInput() throws Exception {
        CreateOrderDto dto = new CreateOrderDto(); // No items

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payOrder_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/orders/1/pay"))
                .andExpect(status().isNoContent());
        Mockito.verify(orderService).payOrder(1L);
    }

    @Test
    void cancelOrder_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isNoContent());
        Mockito.verify(orderService).cancelOrder(1L);
    }
}