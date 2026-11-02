package com.project.webhookplatform.config;

import com.project.webhookplatform.service.RetryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;

@Configuration
@EnableScheduling
public class DeliveryConfig {

    @Value("${delivery.retry.max-attempts:5}")
    private int maxAttempts;

    @Value("${delivery.retry.base-delay-ms:1000}")
    private long baseDelayMs;

    @Value("${delivery.retry.cap-delay-ms:30000}")
    private long capDelayMs;

    @Bean
    public RetryPolicy retryPolicy() {
        return new RetryPolicy(maxAttempts, Duration.ofMillis(baseDelayMs), Duration.ofMillis(capDelayMs));
    }
}
