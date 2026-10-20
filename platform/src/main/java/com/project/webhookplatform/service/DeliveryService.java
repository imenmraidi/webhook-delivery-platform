package com.project.webhookplatform.service;

import com.project.webhookplatform.model.Delivery;
import com.project.webhookplatform.model.Event;
import com.project.webhookplatform.model.WebhookEndpoint;
import com.project.webhookplatform.repository.DeliveryRepository;
import com.project.webhookplatform.repository.EndpointRepository;
import com.project.webhookplatform.repository.EventRepository;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final DeliveryRepository deliveries;
    private final EndpointRepository endpoints;
    private final EventRepository events;
    private final RestClient http;

    public DeliveryService(DeliveryRepository deliveries, EndpointRepository endpoints,
            EventRepository events) {
        this.deliveries = deliveries;
        this.endpoints = endpoints;
        this.events = events;

        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(5))
                        .build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.http = RestClient.builder().requestFactory(factory).build();
    }

    @Async("deliveryExecutor")
    public void attempt(UUID deliveryId) {
        long t0 = System.nanoTime();
        log.info("attempt start delivery={} thread={}", deliveryId, Thread.currentThread().getName());

        Delivery d = deliveries.findById(deliveryId).orElseThrow();
        WebhookEndpoint ep = endpoints.findById(d.getEndpointId()).orElseThrow();
        Event event = events.findById(d.getEventId()).orElseThrow();
        log.info("attempt loaded delivery={} url={} eventType={} afterMs={}",
                deliveryId, ep.getUrl(), event.getType(), ms(t0));

        try {
            var response = http.post()
                    .uri(ep.getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Event-Id", event.getId().toString())
                    .header("X-Event-Type", event.getType())
                    .body(event.getPayload())
                    .retrieve()
                    .toBodilessEntity();
            log.info("attempt response delivery={} status={} afterMs={}",
                    deliveryId, response.getStatusCode().value(), ms(t0));
            d.recordResult(response.getStatusCode().value(), null);
        } catch (RestClientResponseException e) {
            log.warn("attempt http error delivery={} status={} afterMs={}",
                    deliveryId, e.getStatusCode().value(), ms(t0));
            d.recordResult(e.getStatusCode().value(), e.getMessage());
        } catch (Exception e) {
            log.warn("attempt transport error delivery={} ex={} msg={} afterMs={}",
                    deliveryId, e.getClass().getSimpleName(), e.getMessage(), ms(t0));
            d.recordResult(0, e.getMessage());
        }
        deliveries.save(d);
        log.info("attempt end delivery={} status={} attemptCount={} lastStatusCode={} totalMs={}",
                deliveryId, d.getStatus(), d.getAttemptCount(), d.getLastStatusCode(), ms(t0));
    }

    private static long ms(long t0) {
        return (System.nanoTime() - t0) / 1_000_000;
    }
}
