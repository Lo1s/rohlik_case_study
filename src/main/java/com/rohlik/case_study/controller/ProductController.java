package com.rohlik.case_study.controller;

import com.rohlik.case_study.dto.CreateProductDto;
import com.rohlik.case_study.dto.ProductDto;
import com.rohlik.case_study.dto.UpdateProductDto;
import com.rohlik.case_study.exception.ProductInActiveOrderException;
import com.rohlik.case_study.service.ProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** List all products **/
    @GetMapping
    @CircuitBreaker(name = "productService", fallbackMethod = "listProductsFallback")
    public ResponseEntity<List<ProductDto>> listProducts() {
        List<ProductDto> products = productService.listProducts();
        return ResponseEntity.ok(products);
    }

    /** Create a new product **/
    @PostMapping
    @CircuitBreaker(name = "productService", fallbackMethod = "createProductFallback")
    public ResponseEntity<ProductDto> createProduct(
            @Valid @RequestBody CreateProductDto dto) {
        ProductDto created = productService.createProduct(dto);
        return ResponseEntity.ok(created);
    }

    /** Update an existing product **/
    @PutMapping("/{id}")
    @CircuitBreaker(name = "productService", fallbackMethod = "updateProductFallback")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductDto dto) {
        ProductDto updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updated);
    }

    /** Delete a product (only if no active orders reference it) **/
    @DeleteMapping("/{id}")
    @CircuitBreaker(name = "productService", fallbackMethod = "deleteProductFallback")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // --- Fallback methods ---

    public ResponseEntity<List<ProductDto>> listProductsFallback(Throwable t) {
        return ResponseEntity.status(503).build();
    }

    public ResponseEntity<ProductDto> createProductFallback(CreateProductDto dto, Throwable t) {
        return ResponseEntity.status(503).build();
    }

    public ResponseEntity<ProductDto> updateProductFallback(Long id, UpdateProductDto dto, Throwable t) {
        return ResponseEntity.status(503).build();
    }

    public ResponseEntity<Void> deleteProductFallback(Long id, Throwable t) {
        if (t instanceof ProductInActiveOrderException) {
            return ResponseEntity.status(400).build(); // Bad request if product is in active orders
        }
        return ResponseEntity.status(503).build();
    }
}
