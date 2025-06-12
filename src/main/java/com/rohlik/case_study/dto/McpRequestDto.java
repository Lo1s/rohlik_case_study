package com.rohlik.case_study.dto;

import jakarta.validation.constraints.NotBlank;

public class McpRequestDto {
    @NotBlank(message = "Command cannot be empty")
    private String command;
    
    private String sessionId;
    
    public McpRequestDto() {}
    
    public McpRequestDto(String command, String sessionId) {
        this.command = command;
        this.sessionId = sessionId;
    }
    
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
