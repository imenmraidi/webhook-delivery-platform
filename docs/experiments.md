# Experiments

Delivery experiments, in order.

## Step 1 — basic crude delivery

Goal: submit an event, platform POSTs to matching endpoints, see the result in the DB.

Built: receiver (`flaky-receiver`, port 8081) with `POST /hooks`, migration `V3__create_deliveries.sql`, `Delivery` entity/repo, `EventService.ingest`.

Verified: `POST /events` created the delivery, called the HTTP endpoint, updated the `deliveries` row. Worked against `http://localhost:8081/hooks`.

## Step 2 — async delivery

Built: `AsyncConfig` (`@EnableAsync`, executor core 10 / max 20 / queue 1000), `DeliveryService.attempt` `@Async`, `EventService` calling it.

Verified: `POST /events` returned in ~15ms while the delivery finished in ~212ms on a `delivery-N` thread. Deliveries landed as `DELIVERED` / 200.

## Step 3 — add Kafka

Delivery moved behind the broker: broker `webhook-kafka` (apache/kafka:3.9.0), `spring-boot-starter-kafka`, topic `webhook.deliveries`, producer/consumer wired.

## Step 4 — basic flow on Kafka-backed delivery

Goal: run the basic flow after Kafka was added, expect it to work the same way.

Result: events sent through `POST /events` were delivered via Kafka and landed as `DELIVERED` / 200.

## Step 5 — failure means failure (killed flaky receiver mid-delivery)

Goal: kill the receiver mid-delivery, expect the delivery to fail cleanly.

Result: delivery went to `FAILED` with a transport error; `last_status_code` stayed null; no stuck `PENDING`.

## Step 6 — killing the app mid-delivery

Goal: kill the platform mid-delivery, restart, expect redelivery and a clean completion.

Result: platform killed mid-delivery, restarted, Kafka redelivered, delivery completed as `DELIVERED` / 200 with attempt 1. No stuck `PENDING`.

## Step 7 — burst & lag (buffering)

Goal: prove producer/consumer decoupling under a burst.

Do: receiver sleep set to 3s, send 30 events quickly via `POST /events`.

Result: burst loop finished in ~1-2s, API stayed fast, all `POST /events` returned 202.

While draining, ran:

```sh
docker run --rm --network host --entrypoint /opt/kafka/bin/kafka-consumer-groups.sh \
  apache/kafka:3.9.0 --bootstrap-server localhost:9092 \
  --describe --group delivery-workers
```

Output:

```text
GROUP            TOPIC              PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID         HOST            CLIENT-ID
delivery-workers webhook.deliveries 0          8               8               0               consumer-delivery-workers-1-aaaa67e1-8ac7-4900-99c7-22376af80aad /172.24.0.1     consumer-delivery-workers-1
delivery-workers webhook.deliveries 1          60              60              0               consumer-delivery-workers-1-aaaa67e1-8ac7-4900-99c7-22376af80aad /172.24.0.1     consumer-delivery-workers-1
delivery-workers webhook.deliveries 2          30              30              0               consumer-delivery-workers-1-aaaa67e1-8ac7-4900-99c7-22376af80aad /172.24.0.1     consumer-delivery-workers-1
```

Then second run showed LAG 0 everywhere. Deliveries went from `PENDING` to `DELIVERED` gradually, ending `DELIVERED` / 200 / attempt 1.

## Current state

- Delivery platform: basic flow, async, Kafka, basic flow on Kafka, failure, kill-mid-delivery, burst/lag all documented.
- Build + tests pass.
- Instrumentation left in place for upcoming flaky-mode experiments.

## Deliveries table (latest)

```text
 id              | event_id | endpoint_id | status    | attempt_count | last_status_code
-----------------+----------+-------------+-----------+---------------+-------------------
 3b048a37...    | ...      | 08976255... | DELIVERED | 1              | 200
```

