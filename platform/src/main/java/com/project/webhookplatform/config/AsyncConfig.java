package com.project.webhookplatform.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("deliveryExecutor")
    public Executor deliveryExecutor() {
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(10);      // threads kept ready
        ex.setMaxPoolSize(20);       // extra threads allowed when the queue is full
        ex.setQueueCapacity(1000);   // tasks waiting for a free thread
        ex.setThreadNamePrefix("delivery-");
        ex.initialize();
        return ex;
    }
}
