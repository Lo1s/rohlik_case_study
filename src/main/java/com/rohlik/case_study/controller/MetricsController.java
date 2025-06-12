package com.rohlik.case_study.controller;

import com.rohlik.case_study.repository.ProductRepository;
import com.rohlik.case_study.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/actuator")
public class MetricsController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Application metrics
        metrics.put("timestamp", LocalDateTime.now());
        metrics.put("uptime", getUptime());
        
        // Business metrics
        Map<String, Object> business = new HashMap<>();
        business.put("total-products", productRepository.count());
        business.put("total-orders", orderRepository.count());
        
        metrics.put("business", business);
        
        // System metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> system = new HashMap<>();
        system.put("memory-total", runtime.totalMemory());
        system.put("memory-free", runtime.freeMemory());
        system.put("memory-used", runtime.totalMemory() - runtime.freeMemory());
        system.put("memory-max", runtime.maxMemory());
        system.put("processors", runtime.availableProcessors());
        
        metrics.put("system", system);
        
        // JVM metrics
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("java-version", System.getProperty("java.version"));
        jvm.put("java-vendor", System.getProperty("java.vendor"));
        jvm.put("os-name", System.getProperty("os.name"));
        jvm.put("os-version", System.getProperty("os.version"));
        
        metrics.put("jvm", jvm);
        
        return metrics;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("application", "UP");
        status.put("database", "UP");
        status.put("timestamp", LocalDateTime.now());
        
        // Quick health checks
        try {
            long productCount = productRepository.count();
            long orderCount = orderRepository.count();
            
            status.put("database-connectivity", "OK");
            status.put("data-access", "OK");
            status.put("quick-stats", Map.of(
                "products", productCount,
                "orders", orderCount
            ));
            
        } catch (Exception e) {
            status.put("database-connectivity", "ERROR");
            status.put("error", e.getMessage());
        }
        
        return status;
    }

    private String getUptime() {
        long uptimeMs = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return String.format("%dd %dh %dm %ds", 
            days, hours % 24, minutes % 60, seconds % 60);
    }
}
