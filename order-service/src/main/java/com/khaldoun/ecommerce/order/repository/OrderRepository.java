package com.khaldoun.ecommerce.order.repository;

import com.khaldoun.ecommerce.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.sellerId = :sellerId ORDER BY o.createdAt DESC")
    Page<Order> findOrdersBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    List<Order> findAllByOrderByCreatedAtDesc();
}
