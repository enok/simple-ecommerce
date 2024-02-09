package com.ecommerce.simple.repository;

import com.ecommerce.simple.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
}
