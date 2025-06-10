package com.rohlik.case_study.controller;

import com.rohlik.case_study.dto.CreateProductDto;
import com.rohlik.case_study.dto.ProductDto;
import com.rohlik.case_study.dto.UpdateProductDto;
import com.rohlik.case_study.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
    public List<ProductDto> listProducts() {
        return productService.listProducts();
    }

    /** Create a new product **/
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(
            @Validated @RequestBody CreateProductDto dto) {
        ProductDto created = productService.createProduct(dto);
        return ResponseEntity.ok(created);
    }

    /** Update an existing product **/
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Validated @RequestBody UpdateProductDto dto) {
        ProductDto updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updated);
    }

    /** Delete a product (only if no active orders reference it) **/
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
