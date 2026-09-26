PROJECT: E-Commerce Microservices Platform (CV Portfolio)

TECH STACK:

- Backend: Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.3 (Oakwood), Maven
- Frontend: React 18 + Vite, React Router v6, Axios, TailwindCSS, React Query
- Auth: Spring Security 6 + JWT (access + refresh tokens)
- Service Discovery: Eureka Server (Spring Cloud Netflix)
- API Gateway: Spring Cloud Gateway Server WebFlux (artifact: spring-cloud-starter-gateway-server-webflux)
- Inter-service: Spring Framework 7 @HttpExchange (modern) OR OpenFeign (still valid) — choose one
- DB: PostgreSQL, Spring Data JPA, Flyway
- Email: JavaMailSender (SMTP - Mailtrap for dev)
- Containerization: Docker + Docker Compose

SERVICES & PORTS:

1. eureka-server :8761
2. api-gateway :8080
3. auth-service :8081
4. user-service :8082
5. product-service :8083
6. order-service :8084
7. notification-service:8085

ROLES:

- CUSTOMER: browse products, place orders, view own orders
- SELLER: manage own products, view orders for own products
- ADMIN: manage users, all products, all orders

SECURITY:

- JWT (HS256), Access token 15min, Refresh token 7 days
- @PreAuthorize on controllers
- Gateway validates JWT at edge, forwards user info via headers

REGISTRATION FLOW:

1. POST /auth/register {email, password, confirmPassword, role}
2. Validate password match + strength
3. Save user (enabled=false), generate confirmation token (UUID, 24h expiry)
4. Send email: http://localhost:5173/confirm?token=xxx
5. User clicks -> POST /auth/confirm -> enabled=true
6. Login only works if enabled=true

GATEWAY CONFIG (CRITICAL):

- Route path: spring.cloud.gateway.server.webflux.routes
- NOT: spring.cloud.gateway.routes

CONVENTIONS:

- Java 21: records for DTOs
- Package: com.khaldoun.ecommerce.<service>
- MapStruct for entity <-> DTO
- @ControllerAdvice for exception handling
- Lombok for boilerplate (not DTOs)
