package com.rohlik.case_study.mcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MCPRequest {
    private String jsonrpc = "2.0";
    private String id;
    private String method;
    private Object params;
}