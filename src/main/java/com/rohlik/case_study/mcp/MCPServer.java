package com.rohlik.case_study.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohlik.case_study.mcp.dto.*;
import com.rohlik.case_study.mcp.tools.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP (Model Context Protocol) Server Implementation
 * Enables AI agents to interact with the e-commerce application through standardized tools
 */
@Component
public class MCPServer extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper;
    private final Map<String, MCPTool> tools;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    public MCPServer(ObjectMapper objectMapper, List<MCPTool> mcpTools) {
        this.objectMapper = objectMapper;
        this.tools = new HashMap<>();
        
        // Register all available tools
        for (MCPTool tool : mcpTools) {
            tools.put(tool.getName(), tool);
        }
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        
        // Send initialization message with available tools
        MCPResponse initResponse = MCPResponse.builder()
            .id("init")
            .result(MCPInitResult.builder()
                .protocolVersion("2024-11-05")
                .capabilities(MCPCapabilities.builder()
                    .tools(MCPToolsCapability.builder()
                        .listChanged(true)
                        .build())
                    .build())
                .serverInfo(MCPServerInfo.builder()
                    .name("rohlik-ecommerce-mcp")
                    .version("1.0.0")
                    .build())
                .build())
            .build();
            
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(initResponse)));
    }
    
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            MCPRequest request = objectMapper.readValue(message.getPayload(), MCPRequest.class);
            MCPResponse response = handleRequest(request);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            MCPResponse errorResponse = MCPResponse.builder()
                .id("error")
                .error(MCPError.builder()
                    .code(-32603)
                    .message("Internal error: " + e.getMessage())
                    .build())
                .build();
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }
    
    private MCPResponse handleRequest(MCPRequest request) {
        switch (request.getMethod()) {
            case "tools/list":
                return handleToolsList(request);
            case "tools/call":
                return handleToolCall(request);
            default:
                return MCPResponse.builder()
                    .id(request.getId())
                    .error(MCPError.builder()
                        .code(-32601)
                        .message("Method not found: " + request.getMethod())
                        .build())
                    .build();
        }
    }
    
    private MCPResponse handleToolsList(MCPRequest request) {
        List<MCPToolDefinition> toolDefinitions = tools.values().stream()
            .map(MCPTool::getDefinition)
            .toList();
            
        return MCPResponse.builder()
            .id(request.getId())
            .result(MCPToolsListResult.builder()
                .tools(toolDefinitions)
                .build())
            .build();
    }
    
    private MCPResponse handleToolCall(MCPRequest request) {
        MCPToolCallParams params = objectMapper.convertValue(request.getParams(), MCPToolCallParams.class);
        String toolName = params.getName();
        
        MCPTool tool = tools.get(toolName);
        if (tool == null) {
            return MCPResponse.builder()
                .id(request.getId())
                .error(MCPError.builder()
                    .code(-32602)
                    .message("Tool not found: " + toolName)
                    .build())
                .build();
        }
        
        try {
            MCPToolResult result = tool.execute(params.getArguments());
            return MCPResponse.builder()
                .id(request.getId())
                .result(MCPToolCallResult.builder()
                    .content(List.of(MCPContent.builder()
                        .type("text")
                        .text(result.getContent())
                        .build()))
                    .isError(result.isError())
                    .build())
                .build();
        } catch (Exception e) {
            return MCPResponse.builder()
                .id(request.getId())
                .error(MCPError.builder()
                    .code(-32603)
                    .message("Tool execution failed: " + e.getMessage())
                    .build())
                .build();
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
    }
}