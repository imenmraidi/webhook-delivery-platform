CREATE TABLE deliveries (
    id               UUID PRIMARY KEY,
    event_id         UUID        NOT NULL REFERENCES events (id),
    endpoint_id      UUID        NOT NULL REFERENCES endpoints (id),
    status           TEXT        NOT NULL DEFAULT 'PENDING',
    attempt_count    INT         NOT NULL DEFAULT 0,
    last_status_code INT,
    last_error       TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (event_id, endpoint_id)
);
