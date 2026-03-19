# User Story: US-001 — Resilient Greek Gods REST client

**As a** developer integrating with the Greek gods REST API  
**I want to** call the Greek gods REST service with rate limiting, retry, and circuit breaker policies on those HTTP calls  
**So that** the client remains stable under latency and failures while returning all gods whose names start with `a`

## Acceptance Criteria

See: [US-001_greek_gods_rest_client_resilience.feature](./US-001_greek_gods_rest_client_resilience.feature)

## Related design

- [ADR-001: Greek Gods REST Client — Functional Requirements](../design/ADR-001_Greek_Gods_REST_Client_Functional_Requirements.md)

## Notes

- Prefer verifying behavior without real network calls (e.g. stub the HTTP server).
- Define explicit connect/read timeouts for every outbound connection.
- Retry policy options (backoff, max attempts, idempotency) should be reviewed against the scenarios below.
- Reference upstream API (for context only): `https://my-json-server.typicode.com/jabrena/latency-problems/greek`
