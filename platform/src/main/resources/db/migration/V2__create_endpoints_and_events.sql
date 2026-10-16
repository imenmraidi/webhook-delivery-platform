CREATE TABLE endpoints (
    id          UUID PRIMARY KEY,
    url         TEXT        NOT NULL,
    event_types TEXT[]      NOT NULL,
    secret      TEXT        NOT NULL,
    status      TEXT        NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE events (
    id         UUID PRIMARY KEY,
    type       TEXT        NOT NULL,
    payload    JSONB       NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_type ON events (type);
