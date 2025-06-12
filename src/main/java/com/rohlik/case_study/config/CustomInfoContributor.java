package com.rohlik.case_study.config;

import org.springframework.boot.actuator.info.Info;
import org.springframework.boot.actuator.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> details = new HashMap<>();
        
        // Application details
        details.put("name", "Rohlik Case Study");
        details.put("description", "Spring Boot E-commerce REST API");
        details.put("startup-time", LocalDateTime.now());
        details.put("framework", "Spring Boot 3.0.2");
        details.put("java-version", System.getProperty("java.version"));
        
        // API details
        Map<String, Object> api = new HashMap<>();
        api.put("base-url", "http://localhost:8080");
        api.put("documentation", "http://localhost:8080/swagger-ui.html");
        api.put("endpoints", Map.of(
            "products", "/products",
            "orders", "/orders",
            "health", "/actuator/health",
            "info", "/actuator/info"
        ));
        details.put("api", api);
        
        // Database details
        Map<String, Object> database = new HashMap<>();
        database.put("type", "H2 (In-Memory)");
        database.put("console", "http://localhost:8080/h2-console");
        database.put("driver", "org.h2.Driver");
        details.put("database", database);
        
        // Development details
        Map<String, Object> development = new HashMap<>();
        development.put("proxy-server", "http://localhost:3000");
        development.put("hot-reload", "Spring Boot DevTools enabled");
        development.put("profile", "development");
        details.put("development", development);
        
        builder.withDetails(details);
    }
}
