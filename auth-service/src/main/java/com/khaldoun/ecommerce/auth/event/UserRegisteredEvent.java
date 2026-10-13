package com.khaldoun.ecommerce.auth.event;

import java.time.Instant;
import java.util.UUID;

// Sent when a user confirms their email. Other services keep their own copy
// of the contact data instead of calling auth-service.
public record UserRegisteredEvent(
                UUID eventId,
                Long userId,
                String email,
                String role,
                Instant occurredAt) {
}
