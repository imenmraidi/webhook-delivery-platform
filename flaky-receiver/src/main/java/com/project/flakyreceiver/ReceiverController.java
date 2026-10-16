package com.project.flakyreceiver;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReceiverController {

	@PostMapping("/hooks")
	public ResponseEntity<Void> receive(@RequestBody String body,
										@RequestHeader HttpHeaders headers) {
		System.out.println("RECEIVED: " + body);
		System.out.println("HEADERS: " + headers);
		return ResponseEntity.ok().build();
	}
}
