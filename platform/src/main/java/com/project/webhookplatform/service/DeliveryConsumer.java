package com.project.webhookplatform.service;

import com.project.webhookplatform.config.KafkaTopicConfig;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryConsumer {

    private final DeliveryService deliveryService;

    public DeliveryConsumer(DeliveryService deliveryService) { this.deliveryService = deliveryService; }

    @KafkaListener(topics = KafkaTopicConfig.DELIVERIES_TOPIC)
    public void onMessage(String deliveryId) {
        deliveryService.attempt(UUID.fromString(deliveryId));
    }
}
