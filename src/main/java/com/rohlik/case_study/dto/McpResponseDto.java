package com.rohlik.case_study.dto;

import java.time.LocalDateTime;
import java.util.List;

public class McpResponseDto {
    private String status;
    private String message;
    private String sessionId;
    private LocalDateTime timestamp;
    private Object result;
    private List<String> actions;
    private List<String> errors;
    
    public McpResponseDto() {
        this.timestamp = LocalDateTime.now();
    }
    
    public McpResponseDto(String status, String message) {
        this();
        this.status = status;
        this.message = message;
    }
    
    public McpResponseDto(String status, String message, String sessionId, Object result, List<String> actions, List<String> errors) {
        this();
        this.status = status;
        this.message = message;
        this.sessionId = sessionId;
        this.result = result;
        this.actions = actions;
        this.errors = errors;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }
    
    public List<String> getActions() {
        return actions;
    }
    
    public void setActions(List<String> actions) {
        this.actions = actions;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
