package com.khaldoun.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailRequest(@NotBlank String token) {
}