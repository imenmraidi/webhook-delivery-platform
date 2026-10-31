package com.project.webhookplatform.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String DELIVERIES_TOPIC = "webhook.deliveries";

    @Bean
    public NewTopic deliveriesTopic() {
        return TopicBuilder.name(DELIVERIES_TOPIC)
                .partitions(3)
                .replicas(1)      // one broker locally
                .build();
    }
}
