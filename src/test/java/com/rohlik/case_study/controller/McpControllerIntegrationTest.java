package com.rohlik.case_study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohlik.case_study.dto.CreateProductDto;
import com.rohlik.case_study.dto.McpRequestDto;
import com.rohlik.case_study.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
@Transactional
public class McpControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        // Create some test products
        productService.createProduct(new CreateProductDto("Apple", 100, 1.50));
        productService.createProduct(new CreateProductDto("Banana", 150, 0.80));
        productService.createProduct(new CreateProductDto("Orange", 75, 2.00));
    }

    @Test
    void testMcpHelp() throws Exception {
        mockMvc.perform(get("/api/mcp/help"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Model Control Protocol (MCP) - Natural Language API"))
                .andExpect(jsonPath("$.examples").exists())
                .andExpect(jsonPath("$.supported_intents").exists());
    }

    @Test
    void testMcpStatus() throws Exception {
        mockMvc.perform(get("/api/mcp/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("Model Control Protocol (MCP)"))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.capabilities").isArray());
    }

    @Test
    void testListProductsCommand() throws Exception {
        McpRequestDto request = new McpRequestDto("list products", "test_session");

        mockMvc.perform(post("/api/mcp/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.actions").isArray());
    }

    @Test
    void testCreateOrderCommand() throws Exception {
        McpRequestDto request = new McpRequestDto("create order for 5 apples and 3 bananas", "test_session");

        mockMvc.perform(post("/api/mcp/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.id").exists())
                .andExpect(jsonPath("$.actions").isArray());
    }

    @Test
    void testCreateOrderWithPaymentCommand() throws Exception {
        McpRequestDto request = new McpRequestDto("buy 2 apples and pay for it", "test_session");

        mockMvc.perform(post("/api/mcp/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.result.id").exists())
                .andExpect(jsonPath("$.result.paid").value(true));
    }

    @Test
    void testUnrecognizedCommand() throws Exception {
        McpRequestDto request = new McpRequestDto("do something random", "test_session");

        mockMvc.perform(post("/api/mcp/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void testEmptyCommand() throws Exception {
        McpRequestDto request = new McpRequestDto("", "test_session");

        mockMvc.perform(post("/api/mcp/command")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
