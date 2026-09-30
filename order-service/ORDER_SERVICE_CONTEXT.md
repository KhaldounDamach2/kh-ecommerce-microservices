═══════════════════════════════════════════════════════════════
WORKING AGREEMENT — READ THIS FIRST
═══════════════════════════════════════════════════════════════

1. DO NOT act autonomously. Wait for explicit tasks.
2. Generate ONLY the files I list. Do NOT create extra files.
3. Do NOT modify any file I didn't explicitly mention.
4. After generating, ACTUALLY CREATE the files on disk using your tools.
5. Do NOT run commands.
6. If unsure, ASK before expanding scope.
7. When in doubt, do less, not more.

Reply with "Ready" when I paste this.
═══════════════════════════════════════════════════════════════

PROJECT: Order Service — microservice in E-Commerce platform

TECH STACK:

- Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.3
- Spring Data JPA, PostgreSQL, Flyway, Lombok, MapStruct
- Spring Cloud OpenFeign (for calling product-service)
- Eureka Discovery Client
- Package root: com.khaldoun.ecommerce.order

AUTH HANDLING:

- JWT validated at API Gateway
- Gateway forwards X-User-Id (Long) and X-User-Role (String) headers
- order-service reads these headers — does NOT parse JWT
- Uses a HeaderAuthFilter (same pattern as product-service) to populate
  Spring Security context from the headers

SERVICE INTERACTIONS:

- order-service calls product-service via Feign client
  (to fetch product details when placing an order)
- Product-service endpoint used: GET /products/{id} (public)

FLYWAY SCHEMA (already created):

orders table:
id BIGSERIAL PK
customer_id BIGINT NOT NULL
total_price NUMERIC(12,2) NOT NULL
status VARCHAR(30) NOT NULL (values: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
created_at TIMESTAMP NOT NULL
updated_at TIMESTAMP NOT NULL

order_items table:
id BIGSERIAL PK
order_id BIGINT FK -> orders(id) ON DELETE CASCADE
product_id BIGINT NOT NULL
product_name VARCHAR(200) NOT NULL (snapshot)
product_image VARCHAR(500) (snapshot)
seller_id BIGINT NOT NULL (snapshot)
unit_price NUMERIC(10,2) NOT NULL (snapshot)
quantity INTEGER NOT NULL
subtotal NUMERIC(12,2) NOT NULL

SERVICE PORT: 8084
DATABASE: postgres on localhost:5435, db=order_db

CONVENTIONS:

- Java 21: records for DTOs
- MapStruct for entity <-> DTO
- @ControllerAdvice for exception handling
- Lombok for entities (not DTOs)
- @PreAuthorize with roles where needed
