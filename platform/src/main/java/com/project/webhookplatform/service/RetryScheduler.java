package com.project.webhookplatform.service;

import com.project.webhookplatform.model.Delivery;
import com.project.webhookplatform.repository.DeliveryRepository;
import com.project.webhookplatform.service.DeliveryPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class RetryScheduler {

    private final DeliveryRepository deliveries;
    private final DeliveryPublisher publisher;

    public RetryScheduler(DeliveryRepository deliveries, DeliveryPublisher publisher) {
        this.deliveries = deliveries;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void requeueDue() {
        for (Delivery d : deliveries.findDue(Instant.now(), PageRequest.of(0, 100))) {
            d.markQueued();
            publisher.publish(d);
        }
    }
}
