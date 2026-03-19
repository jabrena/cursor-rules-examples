# ADR-001: Greek Gods REST Client Functional Requirements

**Status:** Proposed
**Date:** Thu Mar 19 20:53:22 CET 2026
**Decisions:** Resilient REST client library using Spring Boot 4, RestClient, and Resilience4j
**Primary interface(s):** REST/HTTP API — Client library for consuming external Greek Gods API

## Context

Building a resilient REST client library to consume the Greek Gods API (`https://my-json-server.typicode.com/jabrena/latency-problems/greek`) with proper handling of latency and failures. The library will be used by other developers in the organization to reliably fetch Greek gods data without having to implement resilience patterns themselves. This addresses the need for stable integration with an external service that may experience intermittent failures, high latency, or rate limiting.

The primary use case is fetching all Greek gods whose names start with 'a', with graceful degradation when resilience mechanisms are triggered (circuit breaker open, retries exhausted, rate limits hit).

## Functional Requirements

### Core Functionality
- **Primary Operation**: Fetch Greek gods whose names start with 'a' from external API
- **Resilience Patterns**: Implement rate limiting, retry policies, and circuit breaker patterns
- **Graceful Degradation**: Return empty list when resilience mechanisms trigger instead of throwing exceptions
- **Developer Experience**: Simple Spring service injectable via dependency injection

### API Design & Consumer Experience
- **Target Users**: Other developers in the organization consuming the library
- **Interface**: Simple Spring service with sensible defaults
- **Error Handling**: No exceptions thrown on resilience failures - graceful degradation with empty list response
- **Configuration**: Timeout settings configurable via Spring properties
- **Testing Support**: Library designed to be testable without external network dependencies

### Input/Output Requirements
- **Input**: No parameters required for the primary operation (filtering for names starting with 'a' is built-in)
- **Output**: List of Greek god objects/names (specific format to be determined during implementation)
- **Failure Response**: Empty list when external service is unavailable or resilience policies are triggered

## Technical Decisions

### Language & Framework
**Decision**: Java 26 with Spring Boot 4
**Rationale**: Leverages modern Java features and the latest Spring Boot capabilities. Java 26 provides performance improvements and language enhancements. Spring Boot 4 offers improved developer experience and updated dependencies.

### REST/HTTP API

#### API Design & Architecture
**Decision**: Simple Spring service with dependency injection, fixed resilience defaults
**Rationale**: Reduces complexity for consuming developers while providing robust resilience out-of-the-box. Developers can inject the service without needing to understand or configure complex resilience patterns.

#### Authentication & Security
**Decision**: No authentication required for the external Greek Gods API
**Rationale**: The upstream API (`https://my-json-server.typicode.com/jabrena/latency-problems/greek`) is publicly accessible and doesn't require authentication.

#### Data & Persistence
**Decision**: 
- **HTTP Client**: RestClient (Spring 6.1+)
- **Resilience Library**: Resilience4j with fixed, well-tested defaults
- **Timeout Configuration**: Connect/read timeouts configurable via Spring properties
**Rationale**: RestClient provides a modern, synchronous HTTP client suitable for this use case. Resilience4j is the industry standard for resilience patterns in Java. Property-based timeout configuration allows operational flexibility without code changes.

#### Integration & Infrastructure
**Decision**: 
- **External Service**: Direct HTTP calls to `https://my-json-server.typicode.com/jabrena/latency-problems/greek`
- **Resilience Policies**: Rate limiting, retry with exponential backoff, circuit breaker
- **Configuration**: Spring properties for timeout settings, hardcoded defaults for resilience policies
**Rationale**: Provides operational flexibility for timeout tuning while maintaining consistent resilience behavior across deployments.

#### Testing & Monitoring
**Decision**: 
- **Testing Strategy**: Testcontainers orchestrating Toxiproxy + WireMock containers for comprehensive resilience testing
- **Test Coverage**: Unit tests for resilience behavior, integration tests with containerized network-level and HTTP-level failure simulation
- **Monitoring**: Standard Spring Boot actuator endpoints for health checks
**Rationale**: Testcontainers provides clean orchestration of Toxiproxy and WireMock containers with proper networking and lifecycle management. Toxiproxy container simulates realistic network conditions (latency, timeouts, connection failures) while WireMock container provides controlled HTTP responses (error codes, malformed JSON). This containerized approach enables complete validation of resilience patterns in an isolated, reproducible test environment without dependency on external services, with automatic cleanup and parallel test execution capabilities.

## Alternatives Considered

### HTTP Client Options
- **RestTemplate**: Rejected due to being in maintenance mode
- **WebClient**: Rejected as reactive programming adds unnecessary complexity for this synchronous use case
- **RestClient**: Selected for modern, synchronous HTTP client capabilities

### Resilience Libraries
- **Spring Retry**: Rejected as it lacks circuit breaker and rate limiting capabilities
- **Hystrix**: Rejected due to being in maintenance mode
- **Resilience4j**: Selected for comprehensive resilience patterns and active maintenance

### Testing Strategies
- **WireMock only**: Rejected as it only mocks HTTP responses, not actual network conditions
- **Toxiproxy only**: Rejected as it doesn't provide controlled HTTP response scenarios
- **Manual container setup**: Rejected due to complexity and inconsistent test environments
- **Testcontainers with Toxiproxy + WireMock**: Selected for comprehensive containerized testing combining network-level failures with controlled HTTP responses

### Error Handling Strategies
- **Exception Throwing**: Rejected as it requires consumers to handle various exception types
- **Result Wrapper**: Rejected as it adds complexity for simple use case
- **Empty List Response**: Selected for graceful degradation and simplified consumer code

## Consequences

### Positive
- **Developer Productivity**: Simple injection-based usage reduces integration complexity
- **Reliability**: Built-in resilience patterns protect against external service failures
- **Testability**: Testcontainers with Toxiproxy + WireMock enables comprehensive resilience testing in isolated containerized environment with both network-level and HTTP-level failure simulation
- **Maintainability**: Fixed defaults reduce configuration complexity while properties allow operational tuning
- **Modern Stack**: Java 26 and Spring Boot 4 provide performance and feature benefits

### Negative
- **Limited Flexibility**: Fixed resilience defaults may not suit all use cases
- **Single Purpose**: Library is specifically designed for Greek gods API, limiting reusability
- **Dependency Weight**: Resilience4j and Spring Boot add dependency overhead for simple use cases

### Follow-up Work
- Define specific resilience policy values (retry attempts, circuit breaker thresholds, rate limits)
- Implement comprehensive test suite with WireMock scenarios
- Create developer documentation and usage examples
- Set up CI/CD pipeline for library distribution
- Define monitoring and alerting for library usage in production

## References

- [US-001: Greek Gods REST Client Resilience](../agile/US-001_Greek_Gods_REST_Client_Resilience.md)
- [Feature File: US-001 Resilience Scenarios](../agile/US-001_greek_gods_rest_client_resilience.feature)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Boot RestClient Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#rest-restclient)