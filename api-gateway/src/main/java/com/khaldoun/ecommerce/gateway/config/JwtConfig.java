package com.khaldoun.ecommerce.gateway.config;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtConfig(String secret) {
}

@Configuration(proxyBeanMethods = false)
class JwtSecretKeyConfiguration {

    @Bean
    SecretKey jwtSecretKey(JwtConfig jwtConfig) {
        return Keys.hmacShaKeyFor(jwtConfig.secret().getBytes(StandardCharsets.UTF_8));
    }
}