package com.project.webhookplatform.repository;

import com.project.webhookplatform.model.Delivery;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
}
