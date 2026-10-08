# 🛒 E-Commerce Microservices Platform

A production-shaped e-commerce platform built with **Spring Boot 4 + Spring Cloud + React 18**, featuring 5 microservices with service discovery, API gateway, JWT authentication, role-based access control, and full Docker orchestration.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.3-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![React](https://img.shields.io/badge/React-18-61dafb)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ed)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)

[![CI](https://github.com/KhaldounDamach2/kh-ecommerce-microservices/actions/workflows/ci.yml/badge.svg)](https://github.com/KhaldounDamach2/kh-ecommerce-microservices/actions/workflows/ci.yml)

---

## 📸 Screenshots

### Customer Flow

| Home                                  | Products                                      | Product Detail                                     |
| ------------------------------------- | --------------------------------------------- | -------------------------------------------------- |
| ![Home](docs/screenshots/01-home.png) | ![Products](docs/screenshots/10-products.png) | ![Detail](docs/screenshots/03-product-details.png) |

| Cart                                  | Checkout                                      | My Orders                                    |
| ------------------------------------- | --------------------------------------------- | -------------------------------------------- |
| ![Cart](docs/screenshots/07-cart.png) | ![Checkout](docs/screenshots/08-checkout.png) | ![Orders](docs/screenshots/09-my-orders.png) |

### Seller Flow

| Seller Products                                    | Product Edit                                  | Order Details                                   |
| -------------------------------------------------- | --------------------------------------------- | ----------------------------------------------- |
| ![Seller](docs/screenshots/02-seller-products.png) | ![Edit](docs/screenshots/05-product-edit.png) | ![Order](docs/screenshots/04-order-details.png) |

| Product Status                                           |     |     |
| -------------------------------------------------------- | --- | --- |
| ![Status](docs/screenshots/06-seller-product-status.png) |     |     |

### Infrastructure & Observability

| Eureka Dashboard                          | Grafana (JVM Metrics)                                 | Prometheus Targets                                        |
| ----------------------------------------- | ----------------------------------------------------- | --------------------------------------------------------- |
| ![Eureka](docs/screenshots/12-eureka.png) | ![Grafana](docs/screenshots/13-grafana-dashboard.png) | ![Prometheus](docs/screenshots/14-prometheus-targets.png) |

| GitHub Actions CI                          |
| ------------------------------------------ |
| ![CI](docs/screenshots/15-ci-pipeline.png) |

---

## 🏗️ Architecture

```
                        ┌──────────────────┐
                        │   Eureka Server  │  :8761
                        │ (Service Registry)│
                        └────────┬─────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │
   ┌────▼─────┐          ┌───────▼───────┐        ┌───────▼───────┐
   │   API    │          │     Auth      │        │   Product     │
   │ Gateway  │          │   Service     │        │   Service     │
   │  :8080   │          │    :8081      │        │    :8083      │
   └────┬─────┘          └───────┬───────┘        └───────┬───────┘
        │                        │                        │
        │                  ┌─────▼──────┐                 │
        │                  │ postgres-  │                 │
        │                  │   auth     │                 │
        │                  │  :5433     │                 │
        │                  └────────────┘                 │
        │                                                 │
        │                  ┌──────────────┐  Feign HC5    │
        └─────────────────►│    Order     │◄──────────────┘
                           │   Service    │
                           │    :8084     │
                           └──────┬───────┘
                                  │
                           ┌──────▼───────┐
                           │ postgres-    │
                           │  order       │
                           │  :5435       │
                           └──────────────┘

   Frontend (React + Vite + nginx) :5173
     └── /api/* proxied to API Gateway :8080
```

---

## 🚀 Quick Start

### Prerequisites

- Docker Desktop (or Docker + Docker Compose)

### Run the entire stack

```bash
git clone <your-repo-url>
cd kh-ecommerce-microservices

# Create .env from template
cp .env.example .env
# Edit .env with your values (JWT secret, Mailtrap credentials)

# Start everything
docker compose up -d
```

Wait ~60 seconds, then:

| Service             | URL                                   |
| ------------------- | ------------------------------------- |
| 🖥️ Frontend         | http://localhost:5173                 |
| 📋 Eureka Dashboard | http://localhost:8761                 |
| 🔌 API Gateway      | http://localhost:8080/api             |
| 📊 Prometheus       | http://localhost:9090                 |
| 📈 Grafana          | http://localhost:3000 (admin/admin)   |
| ❤️ Auth Health      | http://localhost:8081/actuator/health |
| ❤️ Product Health   | http://localhost:8083/actuator/health |
| ❤️ Order Health     | http://localhost:8084/actuator/health |

### Stop

```bash
docker compose down
```

### Stop and delete data

```bash
docker compose down -v
```

---

## 🧩 Services

| Service             | Port | Purpose                                                | Database               |
| ------------------- | ---- | ------------------------------------------------------ | ---------------------- |
| **eureka-server**   | 8761 | Service registry                                       | —                      |
| **api-gateway**     | 8080 | Edge routing, JWT validation, CORS                     | —                      |
| **auth-service**    | 8081 | Register, login, JWT issue/refresh, email confirmation | postgres-auth :5433    |
| **product-service** | 8083 | Product CRUD (SELLER), browse/search (all), stock      | postgres-product :5434 |
| **order-service**   | 8084 | Order placement, history, seller management            | postgres-order :5435   |
| **frontend**        | 5173 | React SPA                                              | —                      |
| **prometheus**      | 9090 | Metrics collection & time-series storage               | —                      |
| **grafana**         | 3000 | Metrics visualization & dashboards                     | —                      |

---

## 📊 Monitoring

Prometheus scrapes metrics from all 5 Spring Boot services; Grafana visualizes them.

| Component      | Purpose                                                             | URL                   |
| -------------- | ------------------------------------------------------------------- | --------------------- |
| **Prometheus** | Metrics collection, time-series storage (15s scrape, 15d retention) | http://localhost:9090 |
| **Grafana**    | Dashboards & visualization (provisioned Prometheus datasource)      | http://localhost:3000 |

Every service exposes `/actuator/prometheus` via Micrometer. Prometheus discovers targets by Docker container name (`eureka-server:8761`, `api-gateway:8080`, `auth-service:8081`, `product-service:8083`, `order-service:8084`).

**Recommended dashboard:** import Grafana dashboard ID `4701` (_JVM (Micrometer)_) via **Dashboards → New → Import**.

## 🚀 CI/CD

**GitHub Actions** runs on every push to `main`: builds all 5 Spring Boot services in parallel (matrix strategy), runs unit tests, and builds the frontend. When everything is green, a `notify` job publishes a deployment signal artifact.

**Continuous deployment** runs on the deployment VM via a polling script + cron:

- Every 5 minutes, the VM queries GitHub's public API for the latest successful `ci.yml` run on `main`
- If the latest green commit differs from the VM's local `HEAD`, it pulls and runs `docker compose up -d --build`
- If checks are still pending or failing, it skips (CI-gated deployment)

This design works **without a public tunnel or SSH secrets** — the VM polls GitHub, so the NAT/firewall constraints of a local VirtualBox deployment are irrelevant.

| Component     | File                       | Purpose                                         |
| ------------- | -------------------------- | ----------------------------------------------- |
| CI workflow   | `.github/workflows/ci.yml` | Matrix build + tests + deployment signal        |
| Deploy script | `~/ci-deploy.sh` (on VM)   | Polls GitHub, verifies checks, deploys on green |
| Cron entry    | `crontab -l` on VM         | Runs deploy script every 5 minutes              |

**Note:** This is a _recreate_ deployment (short downtime during each deploy). For zero-downtime, blue-green deployment would be the next step.

## 🔐 Authentication & Authorization

**JWT validated at the API Gateway** — single point of auth.

### Flow

1. Browser → Gateway with `Authorization: Bearer <JWT>`
2. Gateway's `JwtAuthFilter`:
   - Skips public paths: `/api/auth/register`, `/login`, `/confirm`, `/refresh`, `/logout`
   - Validates JWT signature (HS256)
   - Extracts `userId` and `role`
   - **Strips** any incoming `X-User-Id`/`X-User-Role` headers (anti-spoofing)
   - Adds trusted `X-User-Id` and `X-User-Role` headers downstream
3. Services trust these headers via `HeaderAuthFilter` → Spring Security context
4. `@PreAuthorize("hasRole('SELLER')")` enforced at service level

### Roles

| Role         | Permissions                                                |
| ------------ | ---------------------------------------------------------- |
| **CUSTOMER** | Browse products, place orders, view own orders             |
| **SELLER**   | Manage own products, view incoming orders, update statuses |
| **ADMIN**    | Manage all users, products, orders                         |

### Registration

1. `POST /auth/register` → save user (`enabled=false`) → send confirmation email
2. User clicks link in email → `POST /auth/confirm` → `enabled=true`
3. Only enabled users can log in

### Tokens

- Access token: 15 min (dev: 1h via local profile override)
- Refresh token: 7 days
- Access token in memory (React state)
- Refresh token in localStorage
- Axios interceptor auto-refreshes on 401

---

## 🗄️ Data Model

### auth-service

- `users` (id, email, password, role, enabled, timestamps)
- `confirmation_tokens` (id, token, user_id, expires_at, confirmed_at)
- `refresh_tokens` (id, token, user_id, expires_at, revoked)

### product-service

- `products` (id, name, description, price, stock, category, image_url, seller_id, active, timestamps)
- **Soft delete** via `active=false`
- Indexes on `seller_id`, `category`, `name`, `active`

### order-service

- `orders` (id, customer_id, total_price, status, timestamps)
- `order_items` (id, order_id FK, product_id, product_name, product_image, seller_id, unit_price, quantity, subtotal)
- **Snapshot pattern**: order items store product data at order time
- **State machine**: `PENDING → CONFIRMED → SHIPPED → DELIVERED`

---

## 🔌 Inter-Service Communication

**OpenFeign + Apache HC5** (for PATCH support).

- `order-service` → `product-service`:
  - `GET /products/{id}` — fetch product for validation
  - `PATCH /products/{id}/stock` — decrement stock after order placed
- **Header propagation**: `FeignConfig.RequestInterceptor` forwards `X-User-Id`/`X-User-Role` automatically
- **Best-effort stock decrement**: failures are logged, don't break order placement

---

## 🛠️ Tech Stack

**Backend**

- Java 21
- Spring Boot 4.1.1
- Spring Cloud 2025.1.3 (Eureka, Gateway, OpenFeign)
- Spring Security 6 + JWT (JJWT 0.12.6)
- Spring Data JPA + Hibernate
- Flyway migrations
- MapStruct 1.6.3
- PostgreSQL 16
- Lombok
- OpenFeign + Apache HC5

**Frontend**

- React 18 + Vite 8
- React Router v6
- Axios (interceptors for JWT + auto-refresh)
- TailwindCSS 3
- Context API (AuthContext, CartContext)

**Infrastructure**

- Docker + Docker Compose
- Nginx (frontend serving + reverse proxy)
- Multi-stage Docker builds (JDK → JRE)
- Prometheus + Micrometer (metrics collection)
- Grafana (dashboards & visualization)

---

## 📁 Project Structure

```
kh-ecommerce-microservices/
├── docker-compose.yaml
├── .env.example
├── README.md
├── PROJECT_CONTEXT.md
├── api-gateway/
├── auth-service/
├── product-service/
├── order-service/
├── eureka-server/
├── frontend/
└── docs/
    └── screenshots/
```

---

## 🌐 API Endpoints

### Auth (`/api/auth/**`)

- `POST /api/auth/register`
- `POST /api/auth/confirm`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

### Products (`/api/products/**`)

- `GET /api/products` — list (public)
- `GET /api/products/{id}` — detail (public)
- `GET /api/products/search?q=` — search (public)
- `GET /api/products/mine` — seller's products (SELLER)
- `POST /api/products` — create (SELLER)
- `PUT /api/products/{id}` — update (SELLER, owns)
- `PATCH /api/products/{id}/stock` — adjust stock (internal)
- `DELETE /api/products/{id}` — soft delete (SELLER, owns)

### Orders (`/api/orders/**`)

- `POST /api/orders` — place order (CUSTOMER)
- `GET /api/orders/mine` — customer's orders (CUSTOMER)
- `GET /api/orders/seller` — incoming orders (SELLER)
- `GET /api/orders/{id}` — detail (auth, ownership checked)
- `PATCH /api/orders/{id}/status` — update status (SELLER)

---

## 🎓 Design Decisions

1. **JWT validated at the edge** — single point of auth; downstream services trust headers
2. **Anti-header-spoofing** — Gateway strips and re-adds identity headers
3. **Database-per-service** — no shared schema, each service owns its data
4. **Soft delete** — `active=false` preserves referential integrity
5. **Snapshot pattern** — order items keep original product data
6. **Best-effort side effects** — email/stock failures don't break core flows
7. **Guest cart** — no login needed to add to cart; login enforced at checkout
8. **Return-to-login flow** — post-login redirect back to intended page
9. **Multi-stage Docker builds** — small runtime images (JRE only)
10. **12-factor secrets** — env vars in Docker, gitignored local configs in dev
11. **Provisioned observability** — Prometheus scrape config and Grafana datasource are committed as files, not clicked through a UI; `docker compose up` yields a working monitoring stack.

---

## 🚧 Roadmap

- [x] Prometheus + Grafana observability
- [x] CI/CD pipeline (GitHub Actions + auto-deploy to Ubuntu VM)
- [ ] Public URL / cloud deployment (Oracle Cloud free tier)
- [ ] Kafka event bus + notification-service
- [ ] Admin panel
- [ ] Image upload (S3)
- [ ] Advanced filters (price range, category)
- [ ] Refresh token rotation
- [ ] SAGA pattern for distributed transactions

---

## 📝 License

MIT (or your preferred license)

---

## 👤 Author

**Khaldoun Damach**

- GitHub: [@KhaldounDamach2](https://github.com/KhaldounDamach2)
