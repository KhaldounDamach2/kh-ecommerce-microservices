# E-Commerce Microservices Platform

A full-stack e-commerce platform built with Spring Boot 4 + React, featuring
5 backend microservices with service discovery, API gateway, JWT auth, and
role-based access control. Fully dockerized.

---

## 🏗️ Architecture

                        ┌──────────────────┐
                        │   Eureka Server  │  :8761
                        │ (Service Registry)│
                        └────────┬─────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │

┌────▼─────┐ ┌───────▼───────┐ ┌───────▼───────┐
│ API │ │ Auth │ │ Product │
│ Gateway │ │ Service │ │ Service │
│ :8080 │ │ :8081 │ │ :8083 │
└────┬─────┘ └───────┬───────┘ └───────┬───────┘
│ │ │
│ │ │
│ ┌─────▼──────┐ │
│ │ postgres- │ │
│ │ auth │ │
│ │ :5433 │ │
│ └────────────┘ │
│ │
│ ┌──────────────┐ Feign HC5 │
└─────────────────►│ Order │◄──────────────┘
│ Service │
│ :8084 │
└──────┬───────┘
│
┌──────▼───────┐
│ postgres- │
│ order │
│ :5435 │
└──────────────┘

Frontend (React + Vite + nginx) :5173
└── /api/\* proxied to API Gateway :8080

---

## 🧩 Services

| Service             | Port | Purpose                                                      | DB                     |
| ------------------- | ---- | ------------------------------------------------------------ | ---------------------- |
| **eureka-server**   | 8761 | Service registry                                             | —                      |
| **api-gateway**     | 8080 | Edge routing + JWT validation + CORS                         | —                      |
| **auth-service**    | 8081 | Register, login, JWT issue/refresh, email confirmation       | postgres-auth :5433    |
| **product-service** | 8083 | Product CRUD (SELLER), browse/search (all), stock adjustment | postgres-product :5434 |
| **order-service**   | 8084 | Order placement, order history, seller order management      | postgres-order :5435   |
| **frontend**        | 5173 | React SPA (auth, products, cart, orders)                     | —                      |

---

## 🔐 Security Model

**JWT validated at the API Gateway** (single point of auth).

Flow:

1. Browser → Gateway with `Authorization: Bearer <JWT>`
2. Gateway's `JwtAuthFilter`:
   - Skips public paths: `/api/auth/register`, `/login`, `/confirm`, `/refresh`, `/logout`
   - Validates JWT signature (HS256)
   - Extracts `userId` and `role`
   - **Strips** any incoming `X-User-Id` / `X-User-Role` headers (prevents spoofing)
   - Adds trusted `X-User-Id` and `X-User-Role` headers
3. Downstream services trust these headers (via `HeaderAuthFilter` → Spring Security context)
4. `@PreAuthorize("hasRole('SELLER')")` etc. enforced at service level

**Roles:** CUSTOMER, SELLER, ADMIN

**Registration flow:**

1. POST /auth/register → save user (enabled=false) → send confirmation email (Mailtrap)
2. User clicks confirmation link → POST /auth/confirm → enabled=true
3. Only enabled users can log in

**Token flow:**

- Access token: 15 min (dev: 1h via `application-local.yaml` override)
- Refresh token: 7 days
- Access token in memory (React state)
- Refresh token in localStorage
- Axios interceptor auto-refreshes on 401

---

## 🗄️ Data Model

**Per-service PostgreSQL** (database-per-service pattern).

### auth-service

- `users` (id, email UNIQUE, password BCrypt, role, enabled, created_at, updated_at)
- `confirmation_tokens` (id, token, user_id, expires_at, confirmed_at, created_at)
- `refresh_tokens` (id, token, user_id, expires_at, revoked, created_at)

### product-service

- `products` (id, name, description, price, stock, category, image_url, seller_id, active, timestamps)
- **Soft delete**: `active=false` instead of physical delete
- All reads filter `active=true`
- Composite index on (seller_id, product_id) for seller-order queries

### order-service

- `orders` (id, customer_id, total_price, status, timestamps)
- `order_items` (id, order_id FK, product_id, product_name, product_image, seller_id, unit_price, quantity, subtotal)
- **Snapshot pattern**: order items store product data at order time (name, price, image) — preserved if product changes later

**Order status state machine:**

PENDING → CONFIRMED → SHIPPED → DELIVERED

Only seller can transition. Invalid transitions return 400.

---

## 🔌 Inter-Service Communication

**OpenFeign** (with Apache HC5 for PATCH support).

- `order-service` → `product-service`:
  - `GET /products/{id}` — fetch product details for validation
  - `PATCH /products/{id}/stock` — decrement stock after order placement
- **Header propagation**: `FeignConfig` `RequestInterceptor` forwards `X-User-Id` and `X-User-Role` on every Feign call
- **Best-effort stock decrement**: if the Feign call fails, log ERROR and continue (order is already placed). Production would use SAGA pattern with compensating transactions.

---

## 🖥️ Frontend

**Stack:** React 18 + Vite 8 + TailwindCSS 3 + React Router v6 + Axios

### Pages

| Route                       | Access   | Purpose                          |
| --------------------------- | -------- | -------------------------------- |
| `/`                         | public   | Home                             |
| `/register`                 | public   | Signup                           |
| `/login`                    | public   | Login (supports `?returnTo=`)    |
| `/confirm?token=`           | public   | Auto-confirm email               |
| `/products`                 | public   | Browse + search                  |
| `/products/:id`             | public   | Detail + Add to Cart             |
| `/cart`                     | public   | Shopping cart (localStorage)     |
| `/checkout`                 | CUSTOMER | Place order                      |
| `/orders/mine`              | CUSTOMER | Order history                    |
| `/orders/:id`               | any auth | Order detail                     |
| `/seller/products`          | SELLER   | Manage own products              |
| `/seller/products/new`      | SELLER   | Add product                      |
| `/seller/products/:id/edit` | SELLER   | Edit product                     |
| `/seller/orders`            | SELLER   | Incoming orders + status updates |
| `/dashboard/customer`       | CUSTOMER | Customer dashboard               |
| `/dashboard/seller`         | SELLER   | Seller dashboard                 |
| `/dashboard/admin`          | ADMIN    | Admin dashboard                  |

### Key patterns

- **Guest cart** in localStorage (key: `ecommerce_cart`) — no login needed to add to cart
- **Login required at checkout** (with return-to-checkout flow)
- **ProtectedRoute** with optional `allowedRoles` prop
- **Axios interceptors**: attach JWT, auto-refresh on 401
- **Three axios instances** (auth, products, orders) sharing token via `setAccessToken()` calls from AuthContext

### Frontend API baseURL strategy

- **Dev mode**: `/api/*` proxied by Vite dev server → `http://localhost:8080`
- **Docker/production**: `/api/*` proxied by nginx → `http://api-gateway:8080`

---

## 🐳 Docker Setup

**Single command runs everything:** `docker compose up -d`

### Containers

- 3 Postgres instances (auth, product, order)
- 5 Spring Boot services (multi-stage Dockerfiles: JDK build → JRE runtime)
- 1 nginx-served React frontend
- Custom bridge network `ecommerce-net`
- Persistent volumes for Postgres data

### Configuration

- Secrets passed via **environment variables** (never baked into images)
- `SPRING_PROFILES_ACTIVE=""` prevents `application-local.yaml` from loading in containers
- **JWT_SECRET must be identical** between `api-gateway` and `auth-service`
- Mail credentials (`MAILTRAP_USERNAME`, `MAILTRAP_PASSWORD`) only on auth-service

### Ports (host ↔ container)

| Service          | Host | Container |
| ---------------- | ---- | --------- |
| frontend         | 5173 | 80        |
| api-gateway      | 8080 | 8080      |
| auth-service     | 8081 | 8081      |
| product-service  | 8083 | 8083      |
| order-service    | 8084 | 8084      |
| eureka-server    | 8761 | 8761      |
| postgres-auth    | 5433 | 5432      |
| postgres-product | 5434 | 5432      |
| postgres-order   | 5435 | 5432      |

---

## 🛠️ Tech Stack Summary

**Backend:**

- Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.3
- Spring Security 6 (JWT, BCrypt, role-based)
- Spring Data JPA, Hibernate
- Flyway (migrations)
- MapStruct (DTO mapping)
- OpenFeign + Apache HC5 (inter-service calls)
- JJWT 0.12.6
- PostgreSQL 16
- Lombok

**Frontend:**

- React 18, Vite 8
- React Router v6
- Axios (with interceptors)
- TailwindCSS 3
- Context API (AuthContext, CartContext)

**Infrastructure:**

- Docker + Docker Compose
- Eureka (service discovery)
- Nginx (frontend serving + API proxy)

---

## 📌 Dev Setup

### Run everything in Docker (recommended)

```bash
docker compose up -d
```

### Frontend: seller token-expiry redirect leaves stale navbar (CONFIRMED)

**Reproduction:** Login as seller → wait 15 min for access token expiry → open
/seller/my-products → page redirects to /login but navbar still shows the seller.

**Confirmed root cause chain:**

1. `productsAxios.js` response interceptor is `(error) => Promise.reject(error)`
   — no refresh attempted, 401 propagates to caller.
2. `MyProducts.jsx:33` (and siblings in EditProduct.jsx, NewProduct.jsx) catch
   the 401 and call `navigate("/login", { replace: true })` — client-side nav,
   no page reload.
3. AuthContext.user remains set (nothing cleared it), so Navbar shows the
   stale user.

**Fix plan (all files identified):**

- A. Delete ordersAxios.js and productsAxios.js
- B. Repoint api/orders.js and api/products.js at shared axios.js
- C. axios.js baseURL /api/auth → /api; api/auth.js paths get /auth prefix
- D. Remove setOrdersAccessToken / setProductsAccessToken from AuthContext.jsx
- E. Remove 401/403 catch blocks in seller pages (become dead code)
- F. When refresh fails, clear AuthContext.user (via event bus or full reload)

**Estimated fix time:** 45–60 min including testing.

**Test plan:** Login as seller → force-expire access token → open /seller/my-products
→ expect: page loads normally (silent refresh, no redirect). Then delete
refreshToken from localStorage → open same page → expect: redirect to /login
AND navbar shows no user.
