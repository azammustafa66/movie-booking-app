CREATE TABLE app_users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE TABLE user_sessions (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    refresh_token_hash  VARCHAR(255)  NOT NULL UNIQUE,
    user_id             BIGINT        NOT NULL REFERENCES app_users (id),
    expires_at          TIMESTAMP     NOT NULL,
    revoked             BOOLEAN       NOT NULL DEFAULT FALSE,
    device_type         VARCHAR(255),
    user_agent          VARCHAR(1000),
    ip_address          VARCHAR(255),
    last_used_at        TIMESTAMP,
    created_at          TIMESTAMP     NOT NULL,
    updated_at          TIMESTAMP     NOT NULL
);

CREATE INDEX idx_user_sessions_user_id ON user_sessions (user_id);
