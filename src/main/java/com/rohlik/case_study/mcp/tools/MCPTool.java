package com.rohlik.case_study.mcp.tools;

import com.rohlik.case_study.mcp.dto.MCPToolDefinition;
import java.util.Map;

/**
 * Interface for MCP tools that can be executed by AI agents
 */
public interface MCPTool {
    String getName();
    MCPToolDefinition getDefinition();
    MCPToolResult execute(Map<String, Object> arguments) throws Exception;
}