package com.project.webhookplatform.web;

import com.project.webhookplatform.model.WebhookEndpoint;
import com.project.webhookplatform.repository.EndpointRepository;
import jakarta.validation.Valid;
import java.security.SecureRandom;
import java.util.HexFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointController {

    private final EndpointRepository repo;
    private final SecureRandom random = new SecureRandom();

    public EndpointController(EndpointRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/endpoints")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterEndpointResponse register(@Valid @RequestBody RegisterEndpointRequest req) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String secret = "whsec_" + HexFormat.of().formatHex(bytes);

        var saved = repo.save(new WebhookEndpoint(req.url(), req.eventTypes(), secret));
        return new RegisterEndpointResponse(
                saved.getId(), saved.getUrl(), saved.getEventTypes(), saved.getSecret());
    }
}
