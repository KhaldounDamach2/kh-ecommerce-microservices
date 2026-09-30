package com.khaldoun.ecommerce.order.repository;

import com.khaldoun.ecommerce.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
