package com.rohlik.case_study.repository;

import com.rohlik.case_study.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
