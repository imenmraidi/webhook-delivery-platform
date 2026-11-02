ALTER TABLE deliveries ADD COLUMN next_attempt_at TIMESTAMPTZ;

CREATE INDEX idx_deliveries_due ON deliveries (next_attempt_at) WHERE status = 'RETRYING';

CREATE TABLE delivery_attempts (
    id              UUID PRIMARY KEY,
    delivery_id    UUID        NOT NULL REFERENCES deliveries (id),
    attempt_number INT         NOT NULL,
    started_at      TIMESTAMPTZ NOT NULL,
    duration_ms     BIGINT      NOT NULL,
    status_code     INT,
    error           TEXT,
    outcome         TEXT        NOT NULL,
    UNIQUE (delivery_id, attempt_number)
);
