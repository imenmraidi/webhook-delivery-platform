package com.project.webhookplatform.web;

import com.project.webhookplatform.service.EventService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, UUID> ingest(@Valid @RequestBody CreateEventRequest req) {
        var saved = eventService.ingest(req.type(), req.payload().toString());
        return Map.of("id", saved.getId());
    }
}
