package com.project.webhookplatform.service;

import com.project.webhookplatform.config.KafkaTopicConfig;
import com.project.webhookplatform.model.Delivery;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeliveryPublisher {

    private final KafkaTemplate<String, String> kafka;

    public DeliveryPublisher(KafkaTemplate<String, String> kafka) { this.kafka = kafka; }

    public void publish(Delivery d) {
        kafka.send(KafkaTopicConfig.DELIVERIES_TOPIC,
                   d.getEndpointId().toString(),    // key → partition
                   d.getId().toString());           // value → what to deliver
    }
}
