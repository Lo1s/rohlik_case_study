package com.rohlik.case_study.service;

import com.rohlik.case_study.dto.*;
import com.rohlik.case_study.entity.Order;
import com.rohlik.case_study.entity.Product;
import com.rohlik.case_study.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class McpService {
    
    private final ProductService productService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    
    public McpService(ProductService productService, OrderService orderService, ProductRepository productRepository) {
        this.productService = productService;
        this.orderService = orderService;
        this.productRepository = productRepository;
    }
    
    @Transactional
    public McpResponseDto processCommand(McpRequestDto request) {
        String command = request.getCommand().toLowerCase();
        List<String> actions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Object result = null;
        
        try {
            // Determine the intent of the command
            if (isCreateOrderCommand(command)) {
                result = processCreateOrderCommand(command, actions, errors);
            } else if (isListProductsCommand(command)) {
                result = processListProductsCommand(actions);
            } else if (isCreateProductCommand(command)) {
                result = processCreateProductCommand(command, actions, errors);
            } else if (isPayOrderCommand(command)) {
                result = processPayOrderCommand(command, actions, errors);
            } else if (isCancelOrderCommand(command)) {
                result = processCancelOrderCommand(command, actions, errors);
            } else {
                errors.add("Command not recognized. Try commands like 'create order for 5 bananas and 3 apples', 'list products', 'pay for order 123', etc.");
                return new McpResponseDto("error", "Command not understood", request.getSessionId(), null, actions, errors);
            }
            
            String status = errors.isEmpty() ? "success" : "partial_success";
            String message = createResponseMessage(actions, errors);
            
            return new McpResponseDto(status, message, request.getSessionId(), result, actions, errors);
            
        } catch (Exception e) {
            errors.add("Error processing command: " + e.getMessage());
            return new McpResponseDto("error", "Failed to process command", request.getSessionId(), null, actions, errors);
        }
    }
    
    private boolean isCreateOrderCommand(String command) {
        return command.contains("create order") || command.contains("order") && (command.contains("for") || command.contains("buy"));
    }
    
    private boolean isListProductsCommand(String command) {
        return command.contains("list") && command.contains("product") || 
               command.contains("show products") || 
               command.contains("available products") ||
               command.contains("what products");
    }
    
    private boolean isCreateProductCommand(String command) {
        return command.contains("create product") || command.contains("add product");
    }
    
    private boolean isPayOrderCommand(String command) {
        return command.contains("pay") && command.contains("order");
    }
    
    private boolean isCancelOrderCommand(String command) {
        return command.contains("cancel") && command.contains("order");
    }
    
    private Object processCreateOrderCommand(String command, List<String> actions, List<String> errors) {
        try {
            // Parse items from the command
            List<ParsedOrderItemDto> parsedItems = parseOrderItems(command);
            
            if (parsedItems.isEmpty()) {
                errors.add("No items found in the command. Please specify products and quantities like '5 bananas and 3 apples'");
                return null;
            }
            
            // Convert to actual order items
            List<CreateOrderDto.OrderItemDto> orderItems = new ArrayList<>();
            List<ProductDto> availableProducts = productService.listProducts();
            
            for (ParsedOrderItemDto parsedItem : parsedItems) {
                Optional<ProductDto> product = findProductByName(availableProducts, parsedItem.getProductName());
                
                if (product.isPresent()) {
                    CreateOrderDto.OrderItemDto orderItem = new CreateOrderDto.OrderItemDto();
                    orderItem.setProductId(product.get().getId());
                    orderItem.setQuantity(parsedItem.getQuantity());
                    orderItems.add(orderItem);
                    
                    actions.add(String.format("Added %d %s to order", parsedItem.getQuantity(), product.get().getName()));
                } else {
                    errors.add(String.format("Product '%s' not found", parsedItem.getProductName()));
                }
            }
            
            if (orderItems.isEmpty()) {
                errors.add("No valid products found to create order");
                return null;
            }
            
            // Create the order
            CreateOrderDto createOrderDto = new CreateOrderDto();
            createOrderDto.setItems(orderItems);
            
            Order order = orderService.createOrder(createOrderDto);
            actions.add(String.format("Order created successfully with ID: %d", order.getId()));
            
            // Auto-pay if command includes payment terms
            if (command.contains("pay") || command.contains("purchase") || command.contains("buy")) {
                try {
                    orderService.payOrder(order.getId());
                    actions.add(String.format("Order %d paid successfully", order.getId()));
                } catch (Exception e) {
                    errors.add(String.format("Order created but payment failed: %s", e.getMessage()));
                }
            } else {
                actions.add(String.format("Order %d created but not yet paid. Use 'pay for order %d' to complete payment.", order.getId(), order.getId()));
            }
            
            return order;
            
        } catch (Exception e) {
            errors.add("Failed to create order: " + e.getMessage());
            return null;
        }
    }
    
    private Object processListProductsCommand(List<String> actions) {
        List<ProductDto> products = productService.listProducts();
        actions.add(String.format("Listed %d available products", products.size()));
        return products;
    }
    
    private Object processCreateProductCommand(String command, List<String> actions, List<String> errors) {
        // This is a simplified version - in a real system you'd want more sophisticated parsing
        errors.add("Create product command parsing not yet implemented. Use the products API directly.");
        return null;
    }
    
    private Object processPayOrderCommand(String command, List<String> actions, List<String> errors) {
        try {
            Long orderId = extractOrderId(command);
            if (orderId == null) {
                errors.add("Order ID not found in command. Please specify like 'pay for order 123'");
                return null;
            }
            
            orderService.payOrder(orderId);
            actions.add(String.format("Payment processed for order %d", orderId));
            return Map.of("orderId", orderId, "status", "paid");
            
        } catch (Exception e) {
            errors.add("Failed to process payment: " + e.getMessage());
            return null;
        }
    }
    
    private Object processCancelOrderCommand(String command, List<String> actions, List<String> errors) {
        try {
            Long orderId = extractOrderId(command);
            if (orderId == null) {
                errors.add("Order ID not found in command. Please specify like 'cancel order 123'");
                return null;
            }
            
            orderService.cancelOrder(orderId);
            actions.add(String.format("Order %d cancelled successfully", orderId));
            return Map.of("orderId", orderId, "status", "cancelled");
            
        } catch (Exception e) {
            errors.add("Failed to cancel order: " + e.getMessage());
            return null;
        }
    }
    
    private List<ParsedOrderItemDto> parseOrderItems(String command) {
        List<ParsedOrderItemDto> items = new ArrayList<>();
        
        // Pattern to match quantities and product names
        // Examples: "5 bananas", "3 apples", "10 bread"
        Pattern pattern = Pattern.compile("(\\d+)\\s+([a-zA-Z]+(?:\\s+[a-zA-Z]+)*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        
        while (matcher.find()) {
            try {
                int quantity = Integer.parseInt(matcher.group(1));
                String productName = matcher.group(2).trim();
                items.add(new ParsedOrderItemDto(productName, quantity));
            } catch (NumberFormatException e) {
                // Skip invalid numbers
            }
        }
        
        return items;
    }
    
    private Optional<ProductDto> findProductByName(List<ProductDto> products, String searchName) {
        String normalizedSearch = searchName.toLowerCase();
        
        // First try exact match
        Optional<ProductDto> exactMatch = products.stream()
                .filter(p -> p.getName().toLowerCase().equals(normalizedSearch))
                .findFirst();
        
        if (exactMatch.isPresent()) {
            return exactMatch;
        }
        
        // Then try partial match
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(normalizedSearch) || 
                           normalizedSearch.contains(p.getName().toLowerCase()))
                .findFirst();
    }
    
    private Long extractOrderId(String command) {
        Pattern pattern = Pattern.compile("order\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
    
    private String createResponseMessage(List<String> actions, List<String> errors) {
        StringBuilder message = new StringBuilder();
        
        if (!actions.isEmpty()) {
            message.append("Actions completed: ").append(String.join(", ", actions));
        }
        
        if (!errors.isEmpty()) {
            if (message.length() > 0) {
                message.append(". ");
            }
            message.append("Errors: ").append(String.join(", ", errors));
        }
        
        return message.toString();
    }
}
