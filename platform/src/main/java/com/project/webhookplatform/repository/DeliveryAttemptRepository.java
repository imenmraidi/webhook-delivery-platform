package com.project.webhookplatform.repository;

import com.project.webhookplatform.model.DeliveryAttempt;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, UUID> {
}
