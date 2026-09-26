package com.khaldoun.ecommerce.auth.dto;

import com.khaldoun.ecommerce.auth.domain.Role;

public record UserResponse(Long id, String email, Role role, boolean enabled) {
}