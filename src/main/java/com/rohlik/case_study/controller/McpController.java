package com.rohlik.case_study.controller;

import com.rohlik.case_study.dto.McpRequestDto;
import com.rohlik.case_study.dto.McpResponseDto;
import com.rohlik.case_study.service.McpService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/mcp")
@CrossOrigin(origins = "*")
public class McpController {
    
    private final McpService mcpService;
    
    public McpController(McpService mcpService) {
        this.mcpService = mcpService;
    }
    
    /**
     * Process natural language commands for order management
     * Examples:
     * - "create order for 5 bananas and 3 apples"
     * - "list products"  
     * - "pay for order 123"
     * - "cancel order 456"
     */
    @PostMapping("/command")
    @CircuitBreaker(name = "mcpService", fallbackMethod = "processCommandFallback")
    public ResponseEntity<McpResponseDto> processCommand(@Valid @RequestBody McpRequestDto request) {
        McpResponseDto response = mcpService.processCommand(request);
        
        // Return appropriate HTTP status based on response
        if ("success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else if ("partial_success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get information about available MCP commands and examples
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = Map.of(
            "title", "Model Control Protocol (MCP) - Natural Language API",
            "description", "Use natural language to interact with the e-commerce system",
            "version", "1.0.0",
            "examples", Map.of(
                "Create Order", Arrays.asList(
                    "create order for 5 bananas and 3 apples",
                    "buy 2 milk and 1 bread",
                    "order 10 apples and pay for it"
                ),
                "List Products", Arrays.asList(
                    "list products",
                    "show available products",
                    "what products do you have"
                ),
                "Payment", Arrays.asList(
                    "pay for order 123",
                    "complete payment for order 456"
                ),
                "Cancel Order", Arrays.asList(
                    "cancel order 123",
                    "cancel my order 456"
                )
            ),
            "supported_intents", Arrays.asList(
                "create_order",
                "list_products", 
                "pay_order",
                "cancel_order"
            ),
            "usage", Map.of(
                "endpoint", "POST /api/mcp/command",
                "request_format", Map.of(
                    "command", "string (required) - Natural language command",
                    "sessionId", "string (optional) - Session identifier for tracking"
                ),
                "response_format", Map.of(
                    "status", "success | partial_success | error",
                    "message", "Human readable description of what happened",
                    "result", "API result (order, products, etc)",
                    "actions", "List of actions that were performed",
                    "errors", "List of any errors that occurred",
                    "timestamp", "When the command was processed"
                )
            )
        );
        
        return ResponseEntity.ok(help);
    }
    
    /**
     * Test endpoint to validate MCP service is working
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = Map.of(
            "service", "Model Control Protocol (MCP)",
            "status", "active",
            "capabilities", Arrays.asList(
                "Natural Language Processing",
                "Order Creation from Text",
                "Product Listing",
                "Order Payment",
                "Order Cancellation"
            ),
            "last_updated", "2024-01-01",
            "version", "1.0.0"
        );
        
        return ResponseEntity.ok(status);
    }
    
    // Fallback method for circuit breaker
    public ResponseEntity<McpResponseDto> processCommandFallback(McpRequestDto request, Throwable t) {
        McpResponseDto response = new McpResponseDto(
            "error", 
            "MCP service is currently unavailable. Please try again later."
        );
        response.setSessionId(request.getSessionId());
        response.setErrors(List.of("Service temporarily unavailable: " + t.getMessage()));
        
        return ResponseEntity.status(503).body(response);
    }
}
