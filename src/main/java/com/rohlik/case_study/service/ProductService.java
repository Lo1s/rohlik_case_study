package com.rohlik.case_study.service;

import com.rohlik.case_study.dto.CreateProductDto;
import com.rohlik.case_study.dto.ProductDto;
import com.rohlik.case_study.dto.UpdateProductDto;
import com.rohlik.case_study.entity.Product;
import com.rohlik.case_study.exception.ResourceNotFoundException;
import com.rohlik.case_study.repository.OrderItemRepository;
import com.rohlik.case_study.repository.OrderRepository;
import com.rohlik.case_study.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    // private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository,
                          OrderRepository orderRepository,
                          OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        // this.orderItemRepository = orderItemRepository;
    }

    public List<ProductDto> listProducts() {
        return productRepository.findAll().stream()
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getQuantity(), p.getPrice()))
                .collect(Collectors.toList());
    }

    public ProductDto createProduct(CreateProductDto dto) {
        Product product = new Product(dto.getName(), dto.getQuantity(), dto.getPrice());
        product = productRepository.save(product);
        return new ProductDto(product.getId(), product.getName(), product.getQuantity(), product.getPrice());
    }

    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setName(dto.getName());
        product.setQuantity(dto.getQuantity());
        product.setPrice(dto.getPrice());
        product = productRepository.save(product);
        return new ProductDto(product.getId(), product.getName(), product.getQuantity(), product.getPrice());
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        // Check if any active (not canceled) orders reference this product
        boolean inActiveOrders = orderRepository.findAll().stream()
                .anyMatch(o -> !o.getCanceled() && o.getItems().stream()
                        .anyMatch(item -> item.getProduct().getId().equals(id)));

        if (inActiveOrders) {
            throw new IllegalStateException("Cannot delete product with active orders: " + id);
        }
        productRepository.delete(product);
    }
}
