CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(150)             NOT NULL,
    email         VARCHAR(254)             NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    last_login_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active     BOOLEAN                  NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_credentials
(
    user_id               UUID PRIMARY KEY,
    password_hash         VARCHAR(255)             NOT NULL,
    credential_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_credentials_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE TABLE phones
(
    id           UUID PRIMARY KEY,
    user_id      UUID        NOT NULL,
    number       VARCHAR(30) NOT NULL,
    city_code    VARCHAR(10) NOT NULL,
    country_code VARCHAR(10) NOT NULL,

    CONSTRAINT fk_phones_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_phones_user_id
    ON phones (user_id);

CREATE TABLE user_sessions
(
    id         UUID PRIMARY KEY,
    user_id    UUID                     NOT NULL,
    token_id   VARCHAR(100)             NOT NULL,
    token_hash VARCHAR(64)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uk_sessions_token_id UNIQUE (token_id),
    CONSTRAINT uk_sessions_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_sessions_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_sessions_user_id
    ON user_sessions (user_id);
