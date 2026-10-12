CREATE TABLE notifications (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    type        VARCHAR(30)  NOT NULL,
    title       VARCHAR(200) NOT NULL,
    message     VARCHAR(500) NOT NULL,
    product_id  BIGINT,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- "my unread notifications" is the most common question (the bell badge)
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
