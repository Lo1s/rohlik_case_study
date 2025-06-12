package com.rohlik.case_study.mcp.tools;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MCPToolResult {
    private String content;
    private boolean isError;
    
    public static MCPToolResult success(String content) {
        return MCPToolResult.builder()
            .content(content)
            .isError(false)
            .build();
    }
    
    public static MCPToolResult error(String content) {
        return MCPToolResult.builder()
            .content(content)
            .isError(true)
            .build();
    }
}