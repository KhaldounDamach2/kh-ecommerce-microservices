package com.khaldoun.ecommerce.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.khaldoun.ecommerce.auth.config.JwtProperties;
import com.khaldoun.ecommerce.auth.domain.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private static final String USER_ID_CLAIM = "userId";
    private static final String ROLE_CLAIM = "role";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String email, Role role) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + jwtProperties.accessTokenExpirationMs());

        return Jwts.builder()
                .subject(email)
                .claim(USER_ID_CLAIM, userId)
                .claim(ROLE_CLAIM, role.name())
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + jwtProperties.refreshTokenExpirationMs());

        return Jwts.builder()
                .subject(userId.toString())
                .claim(USER_ID_CLAIM, userId)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get(USER_ID_CLAIM, Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get(ROLE_CLAIM, String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return REFRESH_TOKEN_TYPE.equals(extractAllClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}