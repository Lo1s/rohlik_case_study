package com.rohlik.case_study.mcp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MCPContent {
    private String type;
    private String text;
}