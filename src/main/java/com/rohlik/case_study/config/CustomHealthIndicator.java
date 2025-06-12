package com.rohlik.case_study.config;

import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            // Check application status
            details.put("timestamp", LocalDateTime.now());
            details.put("application", "Rohlik Case Study");
            details.put("version", "1.0.0");
            details.put("environment", "development");
            
            // Check system resources
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            Map<String, Object> memory = new HashMap<>();
            memory.put("total", totalMemory);
            memory.put("free", freeMemory);
            memory.put("used", usedMemory);
            memory.put("max", runtime.maxMemory());
            
            details.put("memory", memory);
            details.put("processors", runtime.availableProcessors());
            
            // Check if memory usage is healthy (less than 80%)
            double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
            if (memoryUsagePercent > 80) {
                return Health.down()
                    .withDetails(details)
                    .withDetail("warning", "High memory usage: " + String.format("%.2f%%", memoryUsagePercent))
                    .build();
            }
            
            return Health.up()
                .withDetails(details)
                .withDetail("status", "All systems operational")
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetails(details)
                .withException(e)
                .build();
        }
    }
}
