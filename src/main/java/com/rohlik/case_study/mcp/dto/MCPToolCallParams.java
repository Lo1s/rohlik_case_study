package com.rohlik.case_study.mcp.dto;

import lombok.Data;
import java.util.Map;

@Data
public class MCPToolCallParams {
    private String name;
    private Map<String, Object> arguments;
}