package com.khaldoun.ecommerce.order.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    RequestInterceptor headerForwardingInterceptor() {
        return requestTemplate -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                HttpServletRequest request = attributes.getRequest();
                String userId = request.getHeader("X-User-Id");
                String userRole = request.getHeader("X-User-Role");

                if (userId != null) {
                    requestTemplate.header("X-User-Id", userId);
                }
                if (userRole != null) {
                    requestTemplate.header("X-User-Role", userRole);
                }
            }
        };
    }
}
