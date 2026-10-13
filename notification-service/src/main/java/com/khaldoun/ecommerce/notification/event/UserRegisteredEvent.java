package com.khaldoun.ecommerce.notification.event;

import java.time.Instant;
import java.util.UUID;

// Copy of auth-service's message format (only the format, not its code).
public record UserRegisteredEvent(
        UUID eventId,
        Long userId,
        String email,
        String role,
        Instant occurredAt) {
}
