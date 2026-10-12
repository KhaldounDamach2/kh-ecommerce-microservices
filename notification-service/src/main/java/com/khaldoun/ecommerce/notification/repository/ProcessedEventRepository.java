package com.khaldoun.ecommerce.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khaldoun.ecommerce.notification.domain.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}