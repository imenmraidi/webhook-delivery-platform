package com.project.webhookplatform.service;

import com.project.webhookplatform.model.Event;
import com.project.webhookplatform.model.WebhookEndpoint;
import com.project.webhookplatform.model.Delivery;
import com.project.webhookplatform.repository.DeliveryRepository;
import com.project.webhookplatform.repository.EndpointRepository;
import com.project.webhookplatform.repository.EventRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class EventService {

    private final EventRepository events;
    private final EndpointRepository endpoints;
    private final DeliveryRepository deliveries;
    private final RestClient http = RestClient.create();

    public EventService(EventRepository events, EndpointRepository endpoints,
            DeliveryRepository deliveries) {
        this.events = events;
        this.endpoints = endpoints;
        this.deliveries = deliveries;
    }

    public Event ingest(String type, String payloadJson) {
        Event event = events.save(new Event(type, payloadJson));

        for (WebhookEndpoint ep : endpoints.findSubscribers(type)) {
            Delivery d = deliveries.save(new Delivery(event.getId(), ep.getId()));
            attempt(d, ep, event);
        }
        return event;
    }

    private void attempt(Delivery d, WebhookEndpoint ep, Event event) {
        try {
            var response = http.post()
                    .uri(ep.getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Event-Id", event.getId().toString())
                    .header("X-Event-Type", event.getType())
                    .body(event.getPayload())
                    .retrieve()
                    .toBodilessEntity();
            d.recordResult(response.getStatusCode().value(), null);
        } catch (RestClientResponseException e) {          // got a response: 4xx/5xx
            d.recordResult(e.getStatusCode().value(), e.getMessage());
        } catch (Exception e) {                            // no response: refused, timeout...
            d.recordResult(0, e.getMessage());
        }
        deliveries.save(d);
    }
}
