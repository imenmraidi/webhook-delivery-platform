package com.project.webhookplatform.service;

import com.project.webhookplatform.model.Delivery;
import com.project.webhookplatform.model.Event;
import com.project.webhookplatform.model.WebhookEndpoint;
import com.project.webhookplatform.repository.DeliveryRepository;
import com.project.webhookplatform.repository.EndpointRepository;
import com.project.webhookplatform.repository.EventRepository;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository events;
    private final EndpointRepository endpoints;
    private final DeliveryRepository deliveries;
    private final DeliveryPublisher publisher;

    public EventService(EventRepository events, EndpointRepository endpoints,
            DeliveryRepository deliveries, DeliveryPublisher publisher) {
        this.events = events;
        this.endpoints = endpoints;
        this.deliveries = deliveries;
        this.publisher = publisher;
    }

    public Event ingest(String type, String payloadJson) {
        Event event = events.save(new Event(type, payloadJson));

        for (WebhookEndpoint ep : endpoints.findSubscribers(type)) {
            Delivery d = deliveries.save(new Delivery(event.getId(), ep.getId()));
            publisher.publish(d);   // was: deliveryService.attempt(d.getId())
        }
        return event;
    }
}
