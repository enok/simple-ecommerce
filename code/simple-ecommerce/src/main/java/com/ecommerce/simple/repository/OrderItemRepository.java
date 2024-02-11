package com.ecommerce.simple.repository;

import com.ecommerce.simple.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    Optional<OrderItem> findByProductIdAndOrderId(Integer productId, Integer orderId);
}
