package com.khaldoun.ecommerce.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khaldoun.ecommerce.auth.dto.AuthResponse;
import com.khaldoun.ecommerce.auth.dto.ConfirmEmailRequest;
import com.khaldoun.ecommerce.auth.dto.LoginRequest;
import com.khaldoun.ecommerce.auth.dto.MessageResponse;
import com.khaldoun.ecommerce.auth.dto.RefreshTokenRequest;
import com.khaldoun.ecommerce.auth.dto.RegisterRequest;
import com.khaldoun.ecommerce.auth.dto.UserResponse;
import com.khaldoun.ecommerce.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration requested for {}", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<MessageResponse> confirmEmail(@Valid @RequestBody ConfirmEmailRequest request) {
        log.info("Email confirmation requested");
        return ResponseEntity.ok(authService.confirmEmail(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login requested for {}", request.email());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Access token refresh requested");
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout requested");
        return ResponseEntity.ok(authService.logout(request.refreshToken()));
    }
}