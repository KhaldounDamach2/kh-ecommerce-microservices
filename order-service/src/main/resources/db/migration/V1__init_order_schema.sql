CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    customer_id  BIGINT         NOT NULL,
    total_price  NUMERIC(12, 2) NOT NULL CHECK (total_price >= 0),
    status       VARCHAR(30)    NOT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT         NOT NULL,
    product_id     BIGINT         NOT NULL,
    product_name   VARCHAR(200)   NOT NULL,
    product_image  VARCHAR(500),
    seller_id      BIGINT         NOT NULL,
    unit_price     NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0),
    quantity       INTEGER        NOT NULL CHECK (quantity > 0),
    subtotal       NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_seller ON order_items(seller_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
CREATE INDEX idx_order_items_seller_product ON order_items(seller_id, product_id);