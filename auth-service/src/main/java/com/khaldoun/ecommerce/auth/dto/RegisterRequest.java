package com.khaldoun.ecommerce.auth.dto;

import com.khaldoun.ecommerce.auth.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank String confirmPassword,
        @NotNull Role role) {

    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
}