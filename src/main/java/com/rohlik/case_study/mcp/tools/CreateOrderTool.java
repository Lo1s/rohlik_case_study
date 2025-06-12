package com.rohlik.case_study.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.mcp.dto.MCPToolDefinition;
import com.rohlik.case_study.service.OrderService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP Tool for creating orders
 * Enables AI agents to create orders with specific product IDs and quantities
 */
@Component
public class CreateOrderTool implements MCPTool {
    
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    
    public CreateOrderTool(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getName() {
        return "create_order";
    }
    
    @Override
    public MCPToolDefinition getDefinition() {
        return MCPToolDefinition.builder()
            .name(getName())
            .description("Create a new order with specified products and quantities. Returns order details including ID and total price.")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "items", Map.of(
                        "type", "array",
                        "description", "List of order items",
                        "items", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "productId", Map.of(
                                    "type", "integer",
                                    "description", "ID of the product to order"
                                ),
                                "quantity", Map.of(
                                    "type", "integer",
                                    "description", "Quantity to order",
                                    "minimum", 1
                                )
                            ),
                            "required", List.of("productId", "quantity")
                        )
                    )
                ),
                "required", List.of("items")
            ))
            .build();
    }
    
    @Override
    public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) arguments.get("items");
            
            if (itemsData == null || itemsData.isEmpty()) {
                return MCPToolResult.error("Order must contain at least one item");
            }
            
            CreateOrderDto orderDto = new CreateOrderDto();
            List<CreateOrderDto.OrderItemDto> items = itemsData.stream()
                .map(itemData -> {
                    CreateOrderDto.OrderItemDto item = new CreateOrderDto.OrderItemDto();
                    item.setProductId(Long.valueOf(itemData.get("productId").toString()));
                    item.setQuantity(Integer.valueOf(itemData.get("quantity").toString()));
                    return item;
                })
                .toList();
            
            orderDto.setItems(items);
            
            Order createdOrder = orderService.createOrder(orderDto);
            
            String result = objectMapper.writeValueAsString(Map.of(
                "success", true,
                "order", Map.of(
                    "id", createdOrder.getId(),
                    "totalPrice", calculateTotalPrice(createdOrder),
                    "status", getOrderStatus(createdOrder),
                    "createdAt", createdOrder.getCreatedAt(),
                    "expirationTime", createdOrder.getCreatedAt().plusMinutes(30),
                    "itemCount", createdOrder.getItems().size()
                )
            ));
            
            return MCPToolResult.success(result);
            
        } catch (Exception e) {
            return MCPToolResult.error("Failed to create order: " + e.getMessage());
        }
    }
    
    private double calculateTotalPrice(Order order) {
        return order.getItems().stream()
            .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
    }
    
    private String getOrderStatus(Order order) {
        if (order.getCanceled()) {
            return "CANCELED";
        } else if (order.getPaid()) {
            return "PAID";
        } else {
            return "PENDING";
        }
    }
}