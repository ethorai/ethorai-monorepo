-- ============================================================
-- V3 — Auth schema for Auth.js v5
-- app_user: therapist accounts (credentials, Google, magic link)
-- oauth_account: OAuth provider links per user
-- verification_token: magic-link one-time tokens
-- ============================================================

CREATE TABLE app_user (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(255),
    email          VARCHAR(255) NOT NULL UNIQUE,
    email_verified TIMESTAMPTZ,
    image          TEXT,
    password_hash  TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_app_user_email ON app_user(email);

CREATE TABLE oauth_account (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID         NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    provider            VARCHAR(50)  NOT NULL,
    provider_account_id VARCHAR(255) NOT NULL,
    access_token        TEXT,
    refresh_token       TEXT,
    expires_at          BIGINT,
    token_type          VARCHAR(50),
    scope               TEXT,
    id_token            TEXT,
    UNIQUE (provider, provider_account_id)
);

CREATE INDEX idx_oauth_account_user ON oauth_account(user_id);

CREATE TABLE verification_token (
    identifier VARCHAR(255) NOT NULL,
    token      TEXT         NOT NULL,
    expires    TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (identifier, token)
);
