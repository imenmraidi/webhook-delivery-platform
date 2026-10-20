package com.project.flakyreceiver;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReceiverController {

	private static final Logger log = LoggerFactory.getLogger(ReceiverController.class);

	@PostMapping("/hooks")
	public ResponseEntity<Void> receive(@RequestBody String body,
										@RequestHeader HttpHeaders headers) {
		long t0 = System.nanoTime();
		log.info("receiver hit at {} body={}", Instant.now(), body);
		System.out.println("RECEIVED: " + body);
		System.out.println("HEADERS: " + headers);
		log.info("receiver responding at {} afterMs={}", Instant.now(), (System.nanoTime() - t0) / 1_000_000);
		return ResponseEntity.ok().build();
	}
}
