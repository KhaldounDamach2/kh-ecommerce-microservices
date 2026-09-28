package com.khaldoun.ecommerce.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String[] PUBLIC_PATH_PREFIXES = {
            "/api/auth/register", "/api/auth/login", "/api/auth/confirm",
            "/api/auth/refresh", "/api/auth/logout", "/actuator", "/eureka"
    };
    private static final String PRODUCTS_PATH_PREFIX = "/api/products";
    private static final String PRODUCTS_MINE_PATH = "/api/products/mine";

    private final SecretKey jwtSecretKey;

    public JwtAuthFilter(SecretKey jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        boolean isGet = exchange.getRequest().getMethod() == HttpMethod.GET;

        if (isPublicPath(path) || (isGet && isPublicProductsRead(path))) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            LOGGER.warn("Request rejected due to a missing or invalid Authorization header: {}", path);
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authorization.substring("Bearer ".length());
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object uidClaim = claims.get("userId");
            Object roleClaim = claims.get("role");
            if (!(uidClaim instanceof Number uidNumber)
                    || !(roleClaim instanceof String role)
                    || role.isBlank()) {
                LOGGER.warn("Request rejected because the token is missing required claims: {}", path);
                return unauthorized(exchange, "Invalid or expired token");
            }

            long userId = uidNumber.longValue();
            ServerHttpRequest authenticatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.remove(USER_ID_HEADER);
                        headers.remove(USER_ROLE_HEADER);
                        headers.set(USER_ID_HEADER, Long.toString(userId));
                        headers.set(USER_ROLE_HEADER, role);
                    })
                    .build();
            LOGGER.debug("Authenticated request for user {} with role {}: {}", userId, role, path);
            return chain.filter(exchange.mutate().request(authenticatedRequest).build());
        } catch (JwtException | IllegalArgumentException exception) {
            LOGGER.warn("Request rejected due to an invalid or expired JWT: {}", path);
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private boolean isPublicPath(String path) {
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }

    // GET /api/products, /api/products/search and /api/products/{id} are public;
    // /mine requires auth
    private boolean isPublicProductsRead(String path) {
        return (path.equals(PRODUCTS_PATH_PREFIX) || path.startsWith(PRODUCTS_PATH_PREFIX + "/"))
                && !path.equals(PRODUCTS_MINE_PATH);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}