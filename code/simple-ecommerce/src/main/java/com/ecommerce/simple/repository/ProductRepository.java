package com.ecommerce.simple.repository;

import com.ecommerce.simple.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
