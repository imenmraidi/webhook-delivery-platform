package com.project.webhookplatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
public class Delivery {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RETRYING = "RETRYING";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_DEAD = "DEAD";

    @Id
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "endpoint_id")
    private UUID endpointId;

    private String status;

    @Column(name = "attempt_count")
    private int attemptCount;

    @Column(name = "last_status_code")
    private Integer lastStatusCode;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Delivery() {
    }

    public Delivery(UUID eventId, UUID endpointId) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.endpointId = endpointId;
        this.status = STATUS_PENDING;
        this.createdAt = this.updatedAt = Instant.now();
    }

    public void markDelivered(int statusCode) {
        attemptCount++;
        lastStatusCode = statusCode;
        lastError = null;
        status = STATUS_DELIVERED;
        nextAttemptAt = null;
        updatedAt = Instant.now();
    }

    public void scheduleRetry(int statusCode, String error, Instant when) {
        attemptCount++;
        lastStatusCode = statusCode == 0 ? null : statusCode;
        lastError = error;
        status = STATUS_RETRYING;
        nextAttemptAt = when;
        updatedAt = Instant.now();
    }

    public void markDead(int statusCode, String error) {
        attemptCount++;
        lastStatusCode = statusCode == 0 ? null : statusCode;
        lastError = error;
        status = STATUS_DEAD;
        nextAttemptAt = null;
        updatedAt = Instant.now();
    }

    public void markQueued() {
        status = STATUS_PENDING;
        nextAttemptAt = null;
        updatedAt = Instant.now();
    }

    public boolean isFinished() {
        return STATUS_DELIVERED.equals(status) || STATUS_DEAD.equals(status);
    }

    public UUID getId() { return id; }

    public UUID getEventId() { return eventId; }

    public UUID getEndpointId() { return endpointId; }

    public String getStatus() { return status; }

    public int getAttemptCount() { return attemptCount; }

    public Integer getLastStatusCode() { return lastStatusCode; }

    public String getLastError() { return lastError; }

    public Instant getNextAttemptAt() { return nextAttemptAt; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}
