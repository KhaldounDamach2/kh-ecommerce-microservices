-- Our own address book, filled from the user-registered topic.
-- user_id is the auth-service id, not generated here.
CREATE TABLE user_contact (
    user_id     BIGINT       PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);
