package com.project.webhookplatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "endpoints")
public class WebhookEndpoint {

    @Id
    private UUID id;

    private String url;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "event_types")
    private List<String> eventTypes;

    private String secret;

    private String status;

    @Column(name = "created_at")
    private Instant createdAt;

    protected WebhookEndpoint() {
    }

    public WebhookEndpoint(String url, List<String> eventTypes, String secret) {
        this.id = UUID.randomUUID();
        this.url = url;
        this.eventTypes = eventTypes;
        this.secret = secret;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    public String getSecret() {
        return secret;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
