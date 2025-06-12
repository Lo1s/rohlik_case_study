package com.rohlik.case_study.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohlik.case_study.mcp.dto.MCPToolDefinition;
import com.rohlik.case_study.service.OrderService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP Tool for paying for orders
 * Enables AI agents to process payments for existing orders
 */
@Component
public class PayOrderTool implements MCPTool {
    
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    
    public PayOrderTool(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getName() {
        return "pay_order";
    }
    
    @Override
    public MCPToolDefinition getDefinition() {
        return MCPToolDefinition.builder()
            .name(getName())
            .description("Process payment for an existing order. Marks the order as paid and completes the transaction.")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "orderId", Map.of(
                        "type", "integer",
                        "description", "ID of the order to pay for"
                    )
                ),
                "required", List.of("orderId")
            ))
            .build();
    }
    
    @Override
    public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
        try {
            Long orderId = Long.valueOf(arguments.get("orderId").toString());
            
            orderService.payOrder(orderId);
            
            String result = objectMapper.writeValueAsString(Map.of(
                "success", true,
                "message", "Payment processed successfully",
                "orderId", orderId,
                "status", "PAID"
            ));
            
            return MCPToolResult.success(result);
            
        } catch (Exception e) {
            return MCPToolResult.error("Failed to process payment: " + e.getMessage());
        }
    }
}