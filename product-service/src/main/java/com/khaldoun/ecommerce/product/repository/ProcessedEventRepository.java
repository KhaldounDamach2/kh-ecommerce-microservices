package com.khaldoun.ecommerce.product.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khaldoun.ecommerce.product.domain.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}