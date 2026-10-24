-- Order notifications point to an order (stock notifications to a product),
-- so the bell can open the right page.
ALTER TABLE notifications ADD COLUMN order_id BIGINT;
