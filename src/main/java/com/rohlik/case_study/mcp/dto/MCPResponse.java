package com.rohlik.case_study.mcp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MCPResponse {
    private String jsonrpc = "2.0";
    private String id;
    private Object result;
    private MCPError error;
}