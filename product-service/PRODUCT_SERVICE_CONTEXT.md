═══════════════════════════════════════════════════════════════
WORKING AGREEMENT — READ THIS FIRST
═══════════════════════════════════════════════════════════════
You are assisting with a step-by-step learning project.

1. DO NOT act autonomously. Wait for explicit tasks.
2. When I give a task, generate ONLY the files I list. Do NOT create extra files.
3. Do NOT modify any file I didn't explicitly mention.
4. After generating, ACTUALLY CREATE the files on disk using your tools.
5. Do NOT run build/install/run commands.
6. If unsure, ASK before expanding scope.
7. When in doubt, do less, not more.

Reply with "Ready" when I paste this, then wait for my task.
═══════════════════════════════════════════════════════════════

PROJECT: Product Service — microservice in E-Commerce platform

TECH STACK:

- Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.3
- Spring Data JPA, PostgreSQL, Flyway, Lombok
- Eureka Discovery Client
- Package root: com.khaldoun.ecommerce.product

AUTH HANDLING (CRITICAL):

- JWT is validated at the API Gateway
- Gateway forwards X-User-Id and X-User-Role headers to downstream services
- product-service READS these headers — it does NOT parse JWT itself
- Access rules:
  - POST /products → SELLER (uses X-User-Id as seller_id)
  - PUT /products/{id} → SELLER, must own the product (product.seller_id == X-User-Id)
  - DELETE /products/{id} → SELLER, must own the product
  - GET /products → public
  - GET /products/{id} → public
  - GET /products/mine → SELLER (returns products where seller_id == X-User-Id)
  - GET /products/search → public

FLYWAY SCHEMA (already created — entities MUST match):
products table:
id BIGSERIAL PK
name VARCHAR(200) NOT NULL
description TEXT
price NUMERIC(10,2) NOT NULL
stock INTEGER NOT NULL DEFAULT 0
category VARCHAR(100)
image_url VARCHAR(500)
seller_id BIGINT NOT NULL
active BOOLEAN NOT NULL DEFAULT TRUE
created_at TIMESTAMP NOT NULL
updated_at TIMESTAMP NOT NULL

SERVICE PORT: 8083
DATABASE: postgres on localhost:5434, db=product_db

CONVENTIONS:

- Java 21: records for DTOs
- MapStruct for entity <-> DTO
- @ControllerAdvice for exception handling
- Lombok for entities (not DTOs)
- @PreAuthorize("hasRole('SELLER')") on write endpoints
