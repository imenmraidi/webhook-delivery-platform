package com.project.webhookplatform.service;

import com.project.webhookplatform.model.Delivery;
import com.project.webhookplatform.model.DeliveryAttempt;
import com.project.webhookplatform.model.Event;
import com.project.webhookplatform.model.WebhookEndpoint;
import com.project.webhookplatform.repository.DeliveryAttemptRepository;
import com.project.webhookplatform.repository.DeliveryRepository;
import com.project.webhookplatform.repository.EndpointRepository;
import com.project.webhookplatform.repository.EventRepository;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final DeliveryRepository deliveries;
    private final DeliveryAttemptRepository attempts;
    private final EndpointRepository endpoints;
    private final EventRepository events;
    private final RestClient http;
    private final RetryPolicy policy;

    public DeliveryService(DeliveryRepository deliveries, DeliveryAttemptRepository attempts,
            EndpointRepository endpoints, EventRepository events, RetryPolicy policy) {
        this.deliveries = deliveries;
        this.attempts = attempts;
        this.endpoints = endpoints;
        this.events = events;
        this.policy = policy;

        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(5))
                        .build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.http = RestClient.builder().requestFactory(factory).build();
    }

    public void attempt(UUID deliveryId) {
        Delivery d = deliveries.findById(deliveryId).orElseThrow();
        if (d.isFinished()) return;

        Instant start = Instant.now();
        int status;
        String error = null;
        try {
            WebhookEndpoint ep = endpoints.findById(d.getEndpointId()).orElseThrow();
            Event event = events.findById(d.getEventId()).orElseThrow();
            var response = http.post()
                    .uri(ep.getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Event-Id", event.getId().toString())
                    .header("X-Event-Type", event.getType())
                    .body(event.getPayload())
                    .retrieve()
                    .toBodilessEntity();
            status = response.getStatusCode().value();
        } catch (RestClientResponseException e) {
            status = e.getStatusCode().value();
            error = e.getMessage();
        } catch (Exception e) {
            status = 0;
            error = e.getMessage();
        }
        long ms = Duration.between(start, Instant.now()).toMillis();

        Outcome outcome = policy.classify(status);
        int attemptNo = d.getAttemptCount() + 1;

        if (outcome == Outcome.SUCCESS) {
            d.markDelivered(status);
        } else if (outcome == Outcome.PERMANENT || policy.exhausted(attemptNo)) {
            d.markDead(status, error);
        } else {
            d.scheduleRetry(status, error, Instant.now().plus(policy.delayAfter(attemptNo)));
        }

        attempts.save(new DeliveryAttempt(d.getId(), attemptNo, start, ms, status, error, outcome.name()));
        deliveries.save(d);
    }
}
