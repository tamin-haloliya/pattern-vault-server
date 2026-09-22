CREATE TABLE refresh_tokens
(
    id         UUID                           NOT NULL,
    user_id    UUID                           NOT NULL,
    token_hash VARCHAR(255)                   NOT NULL,
    expire_at  TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    revoked    BOOLEAN                        NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id)
);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT FK_REFRESH_TOKENS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);