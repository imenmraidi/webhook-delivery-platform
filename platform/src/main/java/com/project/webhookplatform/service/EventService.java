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
    private final DeliveryService deliveryService;

    public EventService(EventRepository events, EndpointRepository endpoints,
            DeliveryRepository deliveries, DeliveryService deliveryService) {
        this.events = events;
        this.endpoints = endpoints;
        this.deliveries = deliveries;
        this.deliveryService = deliveryService;
    }

    public Event ingest(String type, String payloadJson) {
        Event event = events.save(new Event(type, payloadJson));

        for (WebhookEndpoint ep : endpoints.findSubscribers(type)) {
            Delivery d = deliveries.save(new Delivery(event.getId(), ep.getId()));
            deliveryService.attempt(d.getId());   // returns immediately
        }
        return event;
    }
}
