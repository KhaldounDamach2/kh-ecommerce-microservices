package com.khaldoun.ecommerce.notification.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.khaldoun.ecommerce.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Newest first, only the caller's own notifications
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // The number on the bell badge
    long countByUserIdAndReadFalse(Long userId);

    // Finds it only if it belongs to this user
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("update Notification n set n.read = true where n.userId = :userId and n.read = false")
    int markAllAsRead(@Param("userId") Long userId);
}
