package com.rohlik.case_study.mcp.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class MCPToolDefinition {
    private String name;
    private String description;
    private Map<String, Object> inputSchema;
}