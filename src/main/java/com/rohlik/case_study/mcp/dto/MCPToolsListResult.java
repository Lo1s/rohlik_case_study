package com.rohlik.case_study.mcp.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MCPToolsListResult {
    private List<MCPToolDefinition> tools;
}