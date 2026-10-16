package com.project.webhookplatform.repository;

import com.project.webhookplatform.model.WebhookEndpoint;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {

    @Query(value = "SELECT * FROM endpoints WHERE status = 'ACTIVE' AND :type = ANY(event_types)",
           nativeQuery = true)
    List<WebhookEndpoint> findSubscribers(@Param("type") String type);
}
