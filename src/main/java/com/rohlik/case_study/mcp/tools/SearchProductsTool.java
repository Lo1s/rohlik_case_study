package com.rohlik.case_study.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohlik.case_study.dto.ProductDto;
import com.rohlik.case_study.mcp.dto.MCPToolDefinition;
import com.rohlik.case_study.service.ProductService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP Tool for searching products by name or description
 * Enables AI agents to find products using natural language
 */
@Component
public class SearchProductsTool implements MCPTool {
    
    private final ProductService productService;
    private final ObjectMapper objectMapper;
    
    public SearchProductsTool(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getName() {
        return "search_products";
    }
    
    @Override
    public MCPToolDefinition getDefinition() {
        return MCPToolDefinition.builder()
            .name(getName())
            .description("Search for products by name or description. Returns product details including ID, name, price, and stock.")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "query", Map.of(
                        "type", "string",
                        "description", "Search query for product name (e.g., 'banana', 'apple', 'orange')"
                    )
                ),
                "required", List.of("query")
            ))
            .build();
    }
    
    @Override
    public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
        String query = (String) arguments.get("query");
        if (query == null || query.trim().isEmpty()) {
            return MCPToolResult.error("Search query is required");
        }
        
        try {
            List<ProductDto> allProducts = productService.listProducts();
            
            // Filter products by name (case-insensitive partial match)
            List<ProductDto> matchingProducts = allProducts.stream()
                .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
            
            if (matchingProducts.isEmpty()) {
                return MCPToolResult.success("No products found matching query: " + query);
            }
            
            String result = objectMapper.writeValueAsString(Map.of(
                "query", query,
                "found", matchingProducts.size(),
                "products", matchingProducts
            ));
            
            return MCPToolResult.success(result);
            
        } catch (Exception e) {
            return MCPToolResult.error("Failed to search products: " + e.getMessage());
        }
    }
}