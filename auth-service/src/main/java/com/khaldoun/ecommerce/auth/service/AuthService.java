package com.khaldoun.ecommerce.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khaldoun.ecommerce.auth.config.JwtProperties;
import com.khaldoun.ecommerce.auth.domain.ConfirmationToken;
import com.khaldoun.ecommerce.auth.domain.RefreshToken;
import com.khaldoun.ecommerce.auth.domain.User;
import com.khaldoun.ecommerce.auth.dto.AuthResponse;
import com.khaldoun.ecommerce.auth.dto.ConfirmEmailRequest;
import com.khaldoun.ecommerce.auth.dto.LoginRequest;
import com.khaldoun.ecommerce.auth.dto.MessageResponse;
import com.khaldoun.ecommerce.auth.dto.RefreshTokenRequest;
import com.khaldoun.ecommerce.auth.dto.RegisterRequest;
import com.khaldoun.ecommerce.auth.dto.UserResponse;
import com.khaldoun.ecommerce.auth.exception.EmailAlreadyExistsException;
import com.khaldoun.ecommerce.auth.exception.InvalidCredentialsException;
import com.khaldoun.ecommerce.auth.exception.InvalidTokenException;
import com.khaldoun.ecommerce.auth.exception.UserNotEnabledException;
import com.khaldoun.ecommerce.auth.repository.ConfirmationTokenRepository;
import com.khaldoun.ecommerce.auth.repository.RefreshTokenRepository;
import com.khaldoun.ecommerce.auth.repository.UserRepository;
import com.khaldoun.ecommerce.auth.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final long CONFIRMATION_TOKEN_VALIDITY_HOURS = 24;

    private final UserRepository userRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final JwtProperties jwtProperties;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request is required");
        }
        if (!request.passwordsMatch()) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String email = request.email();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.role() == null) {
            throw new IllegalArgumentException("Role is required");
        }
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .enabled(false)
                .build();
        user = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .token(token)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plusHours(CONFIRMATION_TOKEN_VALIDITY_HOURS))
                .build();
        confirmationTokenRepository.save(confirmationToken);

        emailService.sendConfirmationEmail(user.getEmail(), token);
        log.info("Registered user {} with role {}", user.getEmail(), user.getRole());
        return toUserResponse(user);
    }

    @Transactional
    public MessageResponse confirmEmail(ConfirmEmailRequest request) {
        if (request == null || request.token() == null || request.token().isBlank()) {
            throw new InvalidTokenException("Invalid confirmation token");
        }

        ConfirmationToken token = confirmationTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new InvalidTokenException("Invalid confirmation token"));
        if (token.getConfirmedAt() != null) {
            throw new InvalidTokenException("Token already used");
        }

        LocalDateTime now = LocalDateTime.now();
        if (token.getExpiresAt().isBefore(now)) {
            throw new InvalidTokenException("Token expired");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new InvalidTokenException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        token.setConfirmedAt(now);
        confirmationTokenRepository.save(token);

        emailService.sendWelcomeEmail(user.getEmail());
        log.info("Email confirmed for user {}", user.getEmail());
        return MessageResponse.of("Email confirmed successfully");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!user.isEnabled()) {
            throw new UserNotEnabledException("Please confirm your email first");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.refreshTokenExpirationMs())))
                .revoked(false)
                .build());

        UserResponse userResponse = toUserResponse(user);
        log.info("User {} logged in", user.getEmail());
        return new AuthResponse(accessToken, refreshToken, "Bearer",
                jwtProperties.accessTokenExpirationMs() / 1_000, userResponse);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        if (refreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token revoked");
        }
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("User not found"));
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(accessToken, refreshToken.getToken(), "Bearer",
                jwtProperties.accessTokenExpirationMs() / 1_000, toUserResponse(user));
    }

    @Transactional
    public MessageResponse logout(String token) {
        if (token != null && !token.isBlank()) {
            refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
                refreshToken.setRevoked(true);
                refreshTokenRepository.save(refreshToken);
            });
        }
        return MessageResponse.of("Logged out");
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.isEnabled());
    }
}