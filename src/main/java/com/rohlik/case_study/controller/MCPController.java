package com.rohlik.case_study.controller;

import com.rohlik.case_study.mcp.tools.MCPTool;
import com.rohlik.case_study.mcp.tools.MCPToolResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for MCP (Model Context Protocol) operations
 * Provides HTTP endpoints for AI agent integration
 */
@RestController
@RequestMapping("/api/mcp")
@CrossOrigin(origins = "*")
public class MCPController {
    
    private final Map<String, MCPTool> tools;
    
    public MCPController(List<MCPTool> mcpTools) {
        this.tools = mcpTools.stream()
            .collect(Collectors.toMap(MCPTool::getName, tool -> tool));
    }
    
    /**
     * List all available MCP tools
     */
    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> listTools() {
        List<Map<String, Object>> toolList = tools.values().stream()
            .map(tool -> Map.of(
                "name", tool.getName(),
                "definition", tool.getDefinition()
            ))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(Map.of(
            "tools", toolList,
            "count", toolList.size()
        ));
    }
    
    /**
     * Execute a specific MCP tool
     */
    @PostMapping("/tools/{toolName}/execute")
    public ResponseEntity<Map<String, Object>> executeTool(
            @PathVariable String toolName,
            @RequestBody Map<String, Object> arguments) {
        
        MCPTool tool = tools.get(toolName);
        if (tool == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Tool not found: " + toolName,
                "availableTools", tools.keySet()
            ));
        }
        
        try {
            MCPToolResult result = tool.execute(arguments);
            return ResponseEntity.ok(Map.of(
                "success", !result.isError(),
                "result", result.getContent(),
                "isError", result.isError()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Tool execution failed: " + e.getMessage(),
                "isError", true
            ));
        }
    }
    
    /**
     * Quick endpoint for natural language order processing
     */
    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> processNaturalLanguageOrder(
            @RequestBody Map<String, String> request) {
        
        String orderRequest = request.get("request");
        if (orderRequest == null || orderRequest.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Order request is required"
            ));
        }
        
        MCPTool naturalLanguageTool = tools.get("natural_language_order");
        if (naturalLanguageTool == null) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Natural language order tool not available"
            ));
        }
        
        try {
            MCPToolResult result = naturalLanguageTool.execute(Map.of("request", orderRequest));
            return ResponseEntity.ok(Map.of(
                "success", !result.isError(),
                "result", result.getContent(),
                "isError", result.isError()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Failed to process order: " + e.getMessage(),
                "isError", true
            ));
        }
    }
}