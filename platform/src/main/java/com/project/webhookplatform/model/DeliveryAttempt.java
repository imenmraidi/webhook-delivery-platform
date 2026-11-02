package com.project.webhookplatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_attempts")
public class DeliveryAttempt {

    @Id
    private UUID id;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "attempt_number")
    private int attemptNumber;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "duration_ms")
    private long durationMs;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "error")
    private String error;

    @Column(name = "outcome")
    private String outcome;

    protected DeliveryAttempt() {
    }

    public DeliveryAttempt(UUID deliveryId, int attemptNumber, Instant startedAt, long durationMs, Integer statusCode, String error, String outcome) {
        this.id = UUID.randomUUID();
        this.deliveryId = deliveryId;
        this.attemptNumber = attemptNumber;
        this.startedAt = startedAt;
        this.durationMs = durationMs;
        this.statusCode = statusCode;
        this.error = error;
        this.outcome = outcome;
    }

    public UUID getId() { return id; }

    public UUID getDeliveryId() { return deliveryId; }

    public int getAttemptNumber() { return attemptNumber; }

    public Instant getStartedAt() { return startedAt; }

    public long getDurationMs() { return durationMs; }

    public Integer getStatusCode() { return statusCode; }

    public String getError() { return error; }

    public String getOutcome() { return outcome; }
}
