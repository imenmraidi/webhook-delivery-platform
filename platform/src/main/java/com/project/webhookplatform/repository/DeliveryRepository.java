package com.project.webhookplatform.repository;

import com.project.webhookplatform.model.Delivery;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    @Query("SELECT d FROM Delivery d WHERE d.status = 'RETRYING' AND d.nextAttemptAt <= :now ORDER BY d.nextAttemptAt")
    List<Delivery> findDue(@Param("now") Instant now, Pageable limit);
}
