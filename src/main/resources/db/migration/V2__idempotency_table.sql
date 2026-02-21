-- Idempotency keys

CREATE TABLE IF NOT EXISTS idempotency_keys (
    id                  UUID DEFAULT uuidv7() PRIMARY KEY,
    idempotency_key     VARCHAR(255) NOT NULL,
    response_body       JSONB,
    status_code         INT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT idempotency_key_uk UNIQUE (idempotency_key)
);