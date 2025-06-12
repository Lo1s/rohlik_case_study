package com.rohlik.case_study.mcp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MCPCapabilities {
    private MCPToolsCapability tools;
}