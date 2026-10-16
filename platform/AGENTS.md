# AGENTS.md

Single-module Spring Boot 4.1.1 app (Java 21, package `com.project.webhookplatform`). Entry: `src/main/java/com/project/webhookplatform/WebhookPlatformApplication.java`.

## Prereqs

- Java 21; use Maven wrapper (`./mvnw` / `mvnw.cmd`), not system `mvn`.
- Postgres must be running before boot or test — config is hardcoded in `src/main/resources/application.yml` (`localhost:5432/webhooks`, user/pass `webhooks`). Run from repo root:
  ```sh
  docker compose up -d postgres
  ```
  then `./mvnw spring-boot:run` inside `platform/` (each app has its own wrapper).

## Verify

- `./mvnw test` — currently only `contextLoads` (`@SpringBootTest`), still requires live DB.
- Focused: `./mvnw -Dtest=ClassName#method test` or `./mvnw -Dtest=ClassName test`.
- Build: `./mvnw package`.

## DB / migrations (Flyway)

- `ddl-auto: validate` — Hibernate never creates schema; every table/column change needs a new `V<N>__*.sql` in `src/main/resources/db/migration/`. Keep entities in `model/` in sync.
- `V1__init.sql` is empty; `V2__create_endpoints_and_events.sql` is the real baseline (`endpoints`, `events` tables); `V3__create_deliveries.sql` adds `deliveries` with `UNIQUE (event_id, endpoint_id)` idempotency.
- Delivery status rule lives in `model/Delivery.recordResult` (2xx → `DELIVERED`, else `FAILED`; statusCode 0 / null code = no HTTP response).
- Postgres-specific columns: `TEXT[]` mapped via `@JdbcTypeCode(SqlTypes.ARRAY)`, `JSONB` via `@JdbcTypeCode(SqlTypes.JSON)`.

## Conventions / gotchas

- Package is `webhookplatform` (no hyphen/underscore — hyphens are illegal in Java packages).
- Spring Boot 4 uses `tools.jackson.databind.JsonNode`, not `com.fasterxml.jackson` (see `web/CreateEventRequest.java`).
- UUIDs and timestamps are assigned in entity constructors (`new Event(...)`, `new WebhookEndpoint(...)`), not via `@GeneratedValue` / `@CreationTimestamp`.
- Endpoint secret format is `whsec_` + 64 hex chars, generated in `EndpointController`; returned once at registration.
- `POST /events` returns `202` with `{"id": ...}`; `POST /endpoints` returns `201`.
- Delivery flow: `web/EventController` → `service/EventService.ingest` (saves event, finds subscribers via `EndpointRepository.findSubscribers`, creates one `Delivery` per match and POSTs synchronously with `RestClient`, headers `X-Event-Id`/`X-Event-Type`). Controllers stay thin — put decisions in the service.
- Sibling test app `../flaky-receiver/` (`server.port` 8081, `POST /hooks` prints body+headers). E2E: boot receiver + platform, register endpoint `http://localhost:8081/hooks`, post event, check `deliveries` row.
- No formatter/linter/CI config — just keep existing style (records for DTOs, constructor injection, Jakarta validation annotations on records).
