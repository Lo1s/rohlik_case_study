package com.rohlik.case_study.config;

import com.rohlik.case_study.mcp.MCPServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for MCP (Model Context Protocol) server
 */
@Configuration
@EnableWebSocket
public class MCPWebSocketConfig implements WebSocketConfigurer {
    
    private final MCPServer mcpServer;
    
    public MCPWebSocketConfig(MCPServer mcpServer) {
        this.mcpServer = mcpServer;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(mcpServer, "/mcp")
                .setAllowedOrigins("*"); // Configure as needed for security
    }
}