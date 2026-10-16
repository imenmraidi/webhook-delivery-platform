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
        this.status = "PENDING";
        this.createdAt = this.updatedAt = Instant.now();
    }

    public void recordResult(int statusCode, String error) {
        this.attemptCount++;
        this.lastStatusCode = statusCode == 0 ? null : statusCode;
        this.lastError = error;
        this.status = (statusCode >= 200 && statusCode < 300) ? "DELIVERED" : "FAILED";
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getEndpointId() {
        return endpointId;
    }

    public String getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Integer getLastStatusCode() {
        return lastStatusCode;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
