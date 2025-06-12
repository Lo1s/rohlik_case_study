package com.rohlik.case_study.config;

import com.rohlik.case_study.entity.Product;
import com.rohlik.case_study.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Initialize sample data for testing MCP integration
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private final ProductRepository productRepository;
    
    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no products exist
        if (productRepository.count() == 0) {
            // Create sample products for testing
            createProduct("Banana", new BigDecimal("0.80"), 150);
            createProduct("Apple", new BigDecimal("1.50"), 100);
            createProduct("Orange", new BigDecimal("2.00"), 75);
            createProduct("Grape", new BigDecimal("3.50"), 50);
            createProduct("Strawberry", new BigDecimal("4.00"), 30);
            createProduct("Pineapple", new BigDecimal("5.00"), 20);
            createProduct("Mango", new BigDecimal("2.50"), 40);
            createProduct("Kiwi", new BigDecimal("1.20"), 60);
        }
    }
    
    private void createProduct(String name, BigDecimal price, int quantity) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price.doubleValue());
        product.setQuantity(quantity);
        productRepository.save(product);
    }
}