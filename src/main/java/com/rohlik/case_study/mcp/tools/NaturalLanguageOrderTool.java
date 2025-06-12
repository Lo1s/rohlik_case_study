package com.rohlik.case_study.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohlik.case_study.dto.CreateOrderDto;
import com.rohlik.case_study.dto.ProductDto;
import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.mcp.dto.MCPToolDefinition;
import com.rohlik.case_study.service.OrderService;
import com.rohlik.case_study.service.ProductService;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MCP Tool for processing natural language order requests
 * Enables AI agents to handle complex requests like "create order for 5 bananas and 4 apples and pay for it"
 */
@Component
public class NaturalLanguageOrderTool implements MCPTool {
    
    private final OrderService orderService;
    private final ProductService productService;
    private final ObjectMapper objectMapper;
    
    // Pattern to match quantity and product name (e.g., "5 bananas", "4 apples")
    private static final Pattern ITEM_PATTERN = Pattern.compile("(\\d+)\\s+([a-zA-Z]+(?:\\s+[a-zA-Z]+)*)", Pattern.CASE_INSENSITIVE);
    
    public NaturalLanguageOrderTool(OrderService orderService, ProductService productService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.productService = productService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getName() {
        return "natural_language_order";
    }
    
    @Override
    public MCPToolDefinition getDefinition() {
        return MCPToolDefinition.builder()
            .name(getName())
            .description("Process natural language order requests. Can create orders, process payments, and handle complex multi-step operations. Examples: 'create order for 5 bananas and 4 apples', 'order 3 oranges and pay for it'")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "request", Map.of(
                        "type", "string",
                        "description", "Natural language order request (e.g., 'create order for 5 bananas and 4 apples and pay for it')"
                    )
                ),
                "required", List.of("request")
            ))
            .build();
    }
    
    @Override
    public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
        try {
            String request = (String) arguments.get("request");
            if (request == null || request.trim().isEmpty()) {
                return MCPToolResult.error("Order request is required");
            }
            
            request = request.toLowerCase().trim();
            
            // Parse the request to extract items
            List<OrderItem> orderItems = parseOrderItems(request);
            if (orderItems.isEmpty()) {
                return MCPToolResult.error("No valid items found in request. Please specify items like '5 bananas and 4 apples'");
            }
            
            // Resolve product names to IDs
            List<ProductDto> allProducts = productService.listProducts();
            List<CreateOrderDto.OrderItemDto> resolvedItems = new ArrayList<>();
            List<String> notFoundProducts = new ArrayList<>();
            
            for (OrderItem item : orderItems) {
                Optional<ProductDto> product = findProductByName(allProducts, item.productName);
                if (product.isPresent()) {
                    CreateOrderDto.OrderItemDto orderItemDto = new CreateOrderDto.OrderItemDto();
                    orderItemDto.setProductId(product.get().getId());
                    orderItemDto.setQuantity(item.quantity);
                    resolvedItems.add(orderItemDto);
                } else {
                    notFoundProducts.add(item.productName);
                }
            }
            
            if (!notFoundProducts.isEmpty()) {
                return MCPToolResult.error("Products not found: " + String.join(", ", notFoundProducts) + 
                    ". Available products: " + allProducts.stream().map(ProductDto::getName).collect(Collectors.joining(", ")));
            }
            
            // Create the order
            CreateOrderDto orderDto = new CreateOrderDto();
            orderDto.setItems(resolvedItems);
            
            Order createdOrder = orderService.createOrder(orderDto);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("action", "order_created");
            result.put("order", Map.of(
                "id", createdOrder.getId(),
                "totalPrice", calculateTotalPrice(createdOrder),
                "status", getOrderStatus(createdOrder),
                "itemCount", createdOrder.getItems().size()
            ));
            
            // Check if payment was requested
            boolean shouldPay = request.contains("pay") || request.contains("payment");
            if (shouldPay) {
                try {
                    orderService.payOrder(createdOrder.getId());
                    result.put("action", "order_created_and_paid");
                    result.put("payment", Map.of(
                        "status", "PAID",
                        "message", "Payment processed successfully"
                    ));
                } catch (Exception e) {
                    result.put("payment", Map.of(
                        "status", "FAILED",
                        "error", "Payment failed: " + e.getMessage()
                    ));
                }
            }
            
            return MCPToolResult.success(objectMapper.writeValueAsString(result));
            
        } catch (Exception e) {
            return MCPToolResult.error("Failed to process natural language order: " + e.getMessage());
        }
    }
    
    private List<OrderItem> parseOrderItems(String request) {
        List<OrderItem> items = new ArrayList<>();
        Matcher matcher = ITEM_PATTERN.matcher(request);
        
        while (matcher.find()) {
            int quantity = Integer.parseInt(matcher.group(1));
            String productName = matcher.group(2).trim();
            
            // Remove plural 's' if present
            if (productName.endsWith("s") && productName.length() > 1) {
                productName = productName.substring(0, productName.length() - 1);
            }
            
            items.add(new OrderItem(quantity, productName));
        }
        
        return items;
    }
    
    private Optional<ProductDto> findProductByName(List<ProductDto> products, String searchName) {
        // First try exact match (case-insensitive)
        Optional<ProductDto> exactMatch = products.stream()
            .filter(p -> p.getName().equalsIgnoreCase(searchName))
            .findFirst();
        
        if (exactMatch.isPresent()) {
            return exactMatch;
        }
        
        // Then try partial match
        return products.stream()
            .filter(p -> p.getName().toLowerCase().contains(searchName.toLowerCase()) ||
                        searchName.toLowerCase().contains(p.getName().toLowerCase()))
            .findFirst();
    }
    
    private static class OrderItem {
        final int quantity;
        final String productName;
        
        OrderItem(int quantity, String productName) {
            this.quantity = quantity;
            this.productName = productName;
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