# AGENTS.md

Single-module Spring Boot 4.1.1 app (Java 21, package `com.project.webhookplatform`). Entry: `src/main/java/com/project/webhookplatform/WebhookPlatformApplication.java`.

## Prereqs

- Java 21; use Maven wrapper (`./mvnw` / `mvnw.cmd`), not system `mvn`.
- Postgres must be running before boot or test — config is hardcoded in `src/main/resources/application.yml` (`localhost:5432/webhooks`, user/pass `webhooks`). Run from repo root:
  ```sh
  docker compose up -d
  ```
  then `./mvnw spring-boot:run` inside `platform/` (each app has its own wrapper).
- Kafka is part of the stack (Kafka plan): `apache/kafka:3.9.0`, container `webhook-kafka`, `PLAINTEXT://localhost:9092`, bootstrap-servers `localhost:9092`.

## Verify

- `./mvnw test` — currently only `contextLoads` (`@SpringBootTest`), still requires live DB.
- Focused: `./mvnw -Dtest=ClassName#method test` or `./mvnw -Dtest=ClassName test`.
- Build: `./mvnw package`.

## DB / migrations (Flyway)

- `ddl-auto: validate` — Hibernate never creates schema; every table/column change needs a new `V<N>__*.sql` in `src/main/resources/db/migration/`. Keep entities in `model/` in sync.
- `V1__init.sql` is empty; `V2__create_endpoints_and_events.sql` is the real baseline (`endpoints`, `events` tables); `V3__create_deliveries.sql` adds `deliveries` with `UNIQUE (event_id, endpoint_id)` idempotency.
- Delivery status rule lives in `model/Delivery.recordResult` (2xx → `DELIVERED`, else `FAILED`; statusCode 0 / null code = no HTTP response).

## Conventions / gotchas

- Package is `webhookplatform` (no hyphen/underscore — hyphens are illegal in Java packages).
- Spring Boot 4 uses `tools.jackson.databind.JsonNode`, not `com.fasterxml.jackson` (see `web/CreateEventRequest.java`).
- UUIDs and timestamps are assigned in entity constructors (`new Event(...)`, `new WebhookEndpoint(...)`), not via `@GeneratedValue` / `@CreationTimestamp`.
- Endpoint secret format is `whsec_` + 64 hex chars, generated in `EndpointController`; returned once at registration.
- `POST /events` returns `202` with `{"id": ...}`; `POST /endpoints` returns `201`.
- Delivery flow: `web/EventController` → `service/EventService.ingest` (saves event, finds subscribers via `EndpointRepository.findSubscribers`, creates one `Delivery` per match, then `service/DeliveryPublisher.publish(d)` → Kafka topic `webhook.deliveries` (key = endpointId, value = deliveryId) → `service/DeliveryConsumer.onMessage` → `service/DeliveryService.attempt(deliveryId)`). Controllers stay thin — put decisions in the service.

- Deliveries are **async via Kafka** (no `@Async` anymore): producer → `webhook.deliveries`; consumer group `delivery-workers` calls `DeliveryService.attempt(deliveryId)`, which re-reads event/endpoint/delivery from the DB and owns the `RestClient` (JDK client pinned to HTTP/1.1, 5s connect + 10s read timeout). It takes a deliveryId, not entities, and bails if the delivery is already `DELIVERED` (idempotency).

- `DeliveryService` owns the only `RestClient` (JDK client pinned to HTTP/1.1, 5s connect + 10s read timeout). Don't reintroduce `RestClient.create()` elsewhere — that variant has no timeout.

## Delivery failure kinds

- **Real HTTP error** → `last_status_code` is set, `status` becomes `FAILED`. E.g. 405 from `example.com` for `POST /hooks`.
- **Transport failure** → `last_status_code` stays `null`, `last_error` carries the reason (timeout / "Request cancelled").

Lesson: "receiver logged RECEIVED" only means the body arrived; delivery success is decided by the response status.

## Kafka notes

- Step 1 of the Kafka plan done: broker `webhook-kafka` (apache/kafka:3.9.0, KRaft) on `localhost:9092`, started via `docker compose up -d`; topic listing verified via `docker exec webhook-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list`.
- Step 2 done: `spring-boot-starter-kafka` added to `platform/pom.xml`; `spring.kafka.bootstrap-servers: localhost:9092`, producer `acks: all` + idempotence, consumer group `delivery-workers` with `auto-offset-reset: earliest` and listener concurrency 3.
- Step 3 done: `KafkaTopicConfig` declares `webhook.deliveries` via `TopicBuilder` (3 partitions, 1 replica); return type is the Apache Kafka admin type `org.apache.kafka.clients.admin.NewTopic`.
- Step 4 done: `DeliveryPublisher` publishes deliveryId keyed by endpointId; `DeliveryConsumer` (`@KafkaListener` on `webhook.deliveries`) calls `DeliveryService.attempt(deliveryId)`.
- Step 5 done: burst/lag experiment — receiver sleep 3s, 30-event burst, LAG drained, deliveries `DELIVERED`.

## Conventions / gotchas

- Package is `webhookplatform` (no hyphen/underscore — hyphens are illegal in Java packages).
- Spring Boot 4 uses `tools.jackson.databind.JsonNode`, not `com.fasterxml.jackson` (see `web/CreateEventRequest.java`).
- UUIDs and timestamps are assigned in entity constructors (`new Event(...)`, `new WebhookEndpoint(...)`), not via `@GeneratedValue` / `@CreationTimestamp`.
- Endpoint secret format is `whsec_` + 64 hex chars, generated in `EndpointController`; returned once at registration.
- `POST /events` returns `202` with `{"id": ...}`; `POST /endpoints` returns `201`.
- Sibling test app `../flaky-receiver/` (`server.port` 8081, `POST /hooks` prints body+headers). E2E: boot receiver + platform, register endpoint `http://localhost:8081/hooks`, post event, check `deliveries` row.
- Run the receiver in a **real terminal**, not with stdout piped to a background process. Its `System.out.println` blocks the Tomcat worker thread when the pipe buffer fills, so the delivery read timeout (10s) fires — deliveries show `last_status_code = null`. Symptom: `POST /hooks` slow but `GET`/404 fast (only the controller path prints).

## Current status

- Kafka delivery implemented and verified end-to-end (basic flow → failure-on-kill → kill-mid-delivery → burst/lag).
- Build + tests pass (`platform`, `flaky-receiver`).
- Instrument delivery attempt via `DeliveryService` / `ReceiverController` logs (`afterMs`) to support upcoming flaky-mode experiments.
- No formatter/linter/CI config — just keep existing style (records for DTOs, constructor injection, Jakarta validation annotations on records).
