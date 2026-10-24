package com.khaldoun.ecommerce.notification.dto;

import java.time.LocalDateTime;

import com.khaldoun.ecommerce.notification.domain.Notification;
import com.khaldoun.ecommerce.notification.domain.NotificationType;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        Long productId,
        Long orderId,
        boolean read,
        LocalDateTime createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getProductId(),
                notification.getOrderId(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}
