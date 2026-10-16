package com.project.webhookplatform.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RegisterEndpointRequest(
        @NotBlank String url,
        @NotEmpty List<String> eventTypes) {
}
