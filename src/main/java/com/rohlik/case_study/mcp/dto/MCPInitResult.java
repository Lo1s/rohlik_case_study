package com.rohlik.case_study.mcp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MCPInitResult {
    private String protocolVersion;
    private MCPCapabilities capabilities;
    private MCPServerInfo serverInfo;
}