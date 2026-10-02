# Project Status

## Services (Spring Boot 4.1.1, Java 21)

- eureka-server :8761
- api-gateway :8080 (JWT filter, CORS)
- auth-service :8081 (postgres-auth :5433)
- product-service :8083 (postgres-product :5434)
- order-service :8084 (postgres-order :5435, Feign HC5)

## Frontend

- React 18 + Vite + Tailwind
- Port 5173
- Routes: /, /login, /register, /confirm, /products, /products/:id,
  /cart, /checkout, /orders/mine, /orders/:id,
  /seller/products (list/new/edit), /seller/orders,
  /dashboard/{customer|seller|admin}

## Infra

- Docker compose: 3 Postgres instances
- Secrets: application-local.yaml (gitignored)
- Feign: openfeign + feign-hc5

## Test credentials

- Customer: test20@example.com / Password123!
- Seller: seller1@example.com / password1!

## TODO (priority order)

1. Dockerize everything (all services + frontend + prometheus later)
2. Prometheus + Grafana
3. Deploy to Oracle Cloud free tier
4. README + screenshots
5. CI/CD GitHub Actions (optional)
6. Kafka + notification-service (optional)

## Notes

- JWT expiry: 1h in dev (application-local.yaml override)
- Stock decrement: best-effort via Feign, no SAGA
- Ghost orders: ids 5,6 have no stock decrement (before feign-hc5 fix)
