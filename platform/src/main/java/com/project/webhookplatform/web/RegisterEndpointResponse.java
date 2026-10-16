package com.project.webhookplatform.web;

import java.util.List;
import java.util.UUID;

public record RegisterEndpointResponse(
        UUID id, String url, List<String> eventTypes, String secret) {
}
