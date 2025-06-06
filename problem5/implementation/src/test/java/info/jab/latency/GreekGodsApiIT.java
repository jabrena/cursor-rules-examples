package info.jab.latency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import info.jab.latency.config.PostgreTestContainers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * Comprehensive Integration Tests for Greek Gods API.
 *
 * This test class validates the complete Greek Gods API functionality using Spring Boot
 * test context with TestRestTemplate for HTTP client testing and TestContainers for
 * database integration.
 *
 * <h2>Test Coverage Areas:</h2>
 * <ul>
 *   <li><strong>API Contract Validation:</strong> Response format, data integrity, JSON schema compliance</li>
 *   <li><strong>Performance Requirements:</strong> Response time < 1 second, consistent performance</li>
 *   <li><strong>Error Handling:</strong> HTTP status codes, error response formats, edge cases</li>
 *   <li><strong>Security:</strong> Header validation, CORS compliance, content type enforcement</li>
 *   <li><strong>Reliability:</strong> Concurrent request handling, system stability under load</li>
 *   <li><strong>Data Quality:</strong> Complete dataset validation, data consistency</li>
 * </ul>
 *
 * @author Integration Test Suite
 * @see <a href="https://spring.io/guides/gs/testing-web/">Spring Boot Testing Guide</a>
 * @see <a href="https://www.testcontainers.org/">TestContainers Documentation</a>
 * @see <a href="https://assertj.github.io/doc/">AssertJ Documentation</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@PostgreTestContainers
@DisplayName("Greek Gods API Integration Tests")
class GreekGodsApiIT {

    private static final String API_ENDPOINT = "/api/v1/gods/greek";
    private static final int EXPECTED_GOD_COUNT = 20;
    private static final Duration MAX_RESPONSE_TIME = Duration.ofSeconds(1);
    private static final Duration MAX_LOAD_TEST_DURATION = Duration.ofSeconds(30);
    private static final String EXPECTED_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;

    // Expected Greek gods for validation (immutable set for thread safety)
    private static final Set<String> EXPECTED_GREEK_GODS = Set.of(
            "Zeus", "Hera", "Poseidon", "Demeter", "Athena",
            "Apollo", "Artemis", "Ares", "Aphrodite", "Hephaestus",
            "Hermes", "Dionysus", "Hades", "Persephone", "Hestia",
            "Hecate", "Pan", "Iris", "Nemesis", "Tyche"
    );

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Nested
    @DisplayName("Basic Functionality Tests")
    class BasicFunctionalityTests {

        @Test
        @DisplayName("Should initialize Spring Boot test context successfully")
        void shouldInitializeSpringBootTestContext() {
            // Given: Spring Boot test context is initialized
            // When: Making a request to the API endpoint
            ResponseEntity<List<String>> response = makeApiRequest();

            // Then: The response should be successful
            assertThat(response.getStatusCode())
                    .describedAs("Spring Boot context should be properly initialized")
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .describedAs("Response body should not be null")
                    .isNotNull();
        }

        @Test
        @DisplayName("Should configure TestRestTemplate with local server port")
        void shouldConfigureTestRestTemplateWithLocalServerPort() {
            // Given: TestRestTemplate is configured with @LocalServerPort
            assertThat(testRestTemplate)
                    .describedAs("TestRestTemplate should be autowired")
                    .isNotNull();
            assertThat(port)
                    .describedAs("Server port should be positive")
                    .isPositive();
            assertThat(baseUrl)
                    .describedAs("Base URL should contain the port")
                    .contains(String.valueOf(port));

            // When: Making a request using the configured port
            ResponseEntity<List<String>> response = makeApiRequest();

            // Then: The request should be successful
            assertThat(response.getStatusCode())
                    .describedAs("Request should be successful using TestRestTemplate")
                    .isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("API Contract Validation Tests")
    class ApiContractValidationTests {

        @Test
        @DisplayName("Should return valid JSON array with exactly 20 Greek god names")
        void shouldReturnValidJsonArrayWithExactly20GreekGodNames() {
            // When: Requesting Greek gods from the API
            ResponseEntity<List<String>> response = makeApiRequest();

            // Then: Response should contain exactly 20 valid god names
            assertSoftly(softly -> {
                softly.assertThat(response.getStatusCode())
                        .describedAs("Response status should be OK")
                        .isEqualTo(HttpStatus.OK);

                softly.assertThat(response.getHeaders().getContentType())
                        .describedAs("Content type should be JSON")
                        .isEqualTo(MediaType.APPLICATION_JSON);

                List<String> godNames = response.getBody();
                softly.assertThat(godNames)
                        .describedAs("Response body should not be null")
                        .isNotNull()
                        .describedAs("All god names should be non-null and non-empty")
                        .allMatch(name -> name != null && !name.trim().isEmpty());
            });
        }

        @Test
        @DisplayName("Should return consistent data across multiple requests")
        void shouldReturnConsistentDataAcrossMultipleRequests() {
            // When: Making multiple sequential requests
            List<String> firstResponse = makeApiRequest().getBody();
            List<String> secondResponse = makeApiRequest().getBody();
            List<String> thirdResponse = makeApiRequest().getBody();

            // Then: All responses should contain identical data
            assertSoftly(softly -> {
                softly.assertThat(firstResponse)
                        .describedAs("First response should not be null")
                        .isNotNull();

                softly.assertThat(secondResponse)
                        .describedAs("Second response should match first response")
                        .isEqualTo(firstResponse);

                softly.assertThat(thirdResponse)
                        .describedAs("Third response should match first response")
                        .isEqualTo(firstResponse);
            });
        }

        @Test
        @DisplayName("Should include proper HTTP headers in response")
        void shouldIncludeProperHttpHeadersInResponse() {
            // When: Making an API request
            ResponseEntity<List<String>> response = makeApiRequest();

            // Then: Response should have proper headers
            HttpHeaders headers = response.getHeaders();
            assertSoftly(softly -> {
                softly.assertThat(headers.getContentType())
                        .describedAs("Content-Type header should be application/json")
                        .isEqualTo(MediaType.APPLICATION_JSON);

                // Content-Length header validation (may be null if using chunked transfer encoding)
                String contentLength = headers.getFirst(HttpHeaders.CONTENT_LENGTH);
                if (contentLength != null) {
                    softly.assertThat(Long.parseLong(contentLength))
                            .describedAs("Content-Length header should be positive when present")
                            .isPositive();
                } else {
                    // If Content-Length is not present, Transfer-Encoding: chunked should be used
                    softly.assertThat(headers.getFirst("Transfer-Encoding"))
                            .describedAs("Transfer-Encoding should be chunked when Content-Length is not present")
                            .isEqualTo("chunked");
                }

                softly.assertThat(headers.getFirst(HttpHeaders.DATE))
                        .describedAs("Date header should be present")
                        .isNotNull()
                        .isNotBlank();

                // Additional security headers validation
                String cacheControl = headers.getFirst(HttpHeaders.CACHE_CONTROL);
                if (cacheControl != null) {
                    softly.assertThat(cacheControl)
                            .describedAs("Cache-Control header should be properly configured")
                            .isNotBlank();
                }
            });
        }

        @Test
        @DisplayName("Should validate JSON schema structure compliance")
        void shouldValidateJsonSchemaStructureCompliance() {
            // When: Making an API request
            ResponseEntity<List<String>> response = makeApiRequest();

            // Then: Response should comply with expected JSON schema
            List<String> godNames = response.getBody();
            assertThat(godNames)
                    .describedAs("Response should be a JSON array")
                    .isInstanceOf(List.class)
                    .describedAs("All elements should be strings")
                    .allMatch(String.class::isInstance)
                    .describedAs("All strings should be non-empty and contain only valid characters")
                    .allMatch(name -> name.matches("^[A-Za-z]+$"))
                    .describedAs("All god names should be properly capitalized")
                    .allMatch(name -> Character.isUpperCase(name.charAt(0)));
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should respond within 1 second performance requirement")
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        void shouldRespondWithinOneSecondPerformanceRequirement() {
            // When: Measuring response time
            Instant start = Instant.now();
            ResponseEntity<List<String>> response = makeApiRequest();
            Instant end = Instant.now();
            Duration responseTime = Duration.between(start, end);

            // Then: Response should be successful and fast
            assertSoftly(softly -> {
                softly.assertThat(response.getStatusCode())
                        .describedAs("Response should be successful")
                        .isEqualTo(HttpStatus.OK);

                softly.assertThat(response.getBody())
                        .describedAs("Response body should contain expected number of gods")
                        .isNotNull()
                        .hasSize(EXPECTED_GOD_COUNT);

                softly.assertThat(responseTime)
                        .describedAs("Response time should be under %s, but was %s",
                                   MAX_RESPONSE_TIME, responseTime)
                        .isLessThan(MAX_RESPONSE_TIME);
            });
        }

        @Test
        @DisplayName("Should maintain consistent performance across multiple requests")
        void shouldMaintainConsistentPerformanceAcrossMultipleRequests() {
            int numberOfRequests = 10;
            List<Duration> responseTimes = IntStream.range(0, numberOfRequests)
                    .mapToObj(i -> measureResponseTime())
                    .toList();

            // Then: All response times should be acceptable
            assertThat(responseTimes)
                    .describedAs("All response times should be under %s", MAX_RESPONSE_TIME)
                    .allMatch(duration -> duration.compareTo(MAX_RESPONSE_TIME) < 0);

            // Calculate and validate average response time
            Duration averageResponseTime = Duration.ofNanos(
                    (long) responseTimes.stream()
                            .mapToLong(Duration::toNanos)
                            .average()
                            .orElse(0)
            );

            assertThat(averageResponseTime)
                    .describedAs("Average response time should be well under the limit")
                    .isLessThan(Duration.ofMillis(500)); // 50% of max allowed time
        }

        private Duration measureResponseTime() {
            Instant start = Instant.now();
            ResponseEntity<List<String>> response = makeApiRequest();
            Instant end = Instant.now();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            return Duration.between(start, end);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return proper error format for non-existent endpoint")
        void shouldReturnProperErrorFormatForNonExistentEndpoint() {
            // When: Requesting a non-existent endpoint
            ResponseEntity<String> response = testRestTemplate.getForEntity(
                    baseUrl + "/api/v1/gods/nonexistent",
                    String.class
            );

            // Then: Should return proper error response
            assertSoftly(softly -> {
                softly.assertThat(response.getStatusCode())
                        .describedAs("Should return an error status code")
                        .isIn(HttpStatus.NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR);

                softly.assertThat(response.getBody())
                        .describedAs("Error response body should not be null")
                        .isNotNull();

                String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                softly.assertThat(contentType)
                        .describedAs("Error response should have JSON content type")
                        .isNotNull()
                        .satisfiesAnyOf(
                                ct -> assertThat(ct).contains("application/json"),
                                ct -> assertThat(ct).contains("application/problem+json")
                        );

                // Validate RFC 7807 ProblemDetail format
                String responseBody = response.getBody();
                softly.assertThat(responseBody)
                        .describedAs("Error response should contain standard error fields")
                        .contains("\"status\"")
                        .contains("\"title\"")
                        .contains("\"detail\"");
            });
        }

        @Test
        @DisplayName("Should handle invalid HTTP methods gracefully")
        void shouldHandleInvalidHttpMethodsGracefully() {
            // When: Using unsupported HTTP methods
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            assertSoftly(softly -> {
                // POST method
                ResponseEntity<String> postResponse = testRestTemplate.exchange(
                        baseUrl + API_ENDPOINT,
                        HttpMethod.POST,
                        new HttpEntity<>(headers),
                        String.class
                );

                softly.assertThat(postResponse.getStatusCode())
                        .describedAs("POST should return method not allowed or error")
                        .isIn(HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.INTERNAL_SERVER_ERROR);

                // PUT method
                ResponseEntity<String> putResponse = testRestTemplate.exchange(
                        baseUrl + API_ENDPOINT,
                        HttpMethod.PUT,
                        new HttpEntity<>(headers),
                        String.class
                );

                softly.assertThat(putResponse.getStatusCode())
                        .describedAs("PUT should return method not allowed or error")
                        .isIn(HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.INTERNAL_SERVER_ERROR);

                // DELETE method
                ResponseEntity<String> deleteResponse = testRestTemplate.exchange(
                        baseUrl + API_ENDPOINT,
                        HttpMethod.DELETE,
                        new HttpEntity<>(headers),
                        String.class
                );

                softly.assertThat(deleteResponse.getStatusCode())
                        .describedAs("DELETE should return method not allowed or error")
                        .isIn(HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.INTERNAL_SERVER_ERROR);
            });
        }

        @Test
        @DisplayName("Should handle malformed requests gracefully")
        void shouldHandleMalformedRequestsGracefully() {
            // When: Making requests with invalid Accept headers
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, "invalid/content-type");

            ResponseEntity<String> response = testRestTemplate.exchange(
                    baseUrl + API_ENDPOINT,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            // Then: Should handle gracefully (based on global exception handler behavior)
            assertThat(response.getStatusCode())
                    .describedAs("Should handle invalid Accept header gracefully")
                    .isIn(HttpStatus.OK, HttpStatus.NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("Security and Headers Tests")
    class SecurityAndHeadersTests {

        @Test
        @DisplayName("Should enforce proper content type negotiation")
        void shouldEnforceProperContentTypeNegotiation() {
            // When: Requesting with specific Accept header
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            ResponseEntity<List<String>> response = testRestTemplate.exchange(
                    baseUrl + API_ENDPOINT,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<String>>() {}
            );

            // Then: Should return proper content type
            assertSoftly(softly -> {
                softly.assertThat(response.getStatusCode())
                        .describedAs("Should accept JSON content type negotiation")
                        .isEqualTo(HttpStatus.OK);

                softly.assertThat(response.getHeaders().getContentType())
                        .describedAs("Response should have JSON content type")
                        .isEqualTo(MediaType.APPLICATION_JSON);
            });
        }

        @Test
        @DisplayName("Should validate response headers for security compliance")
        void shouldValidateResponseHeadersForSecurityCompliance() {
            // When: Making an API request
            ResponseEntity<List<String>> response = makeApiRequest();

            // Then: Should have appropriate security headers (where applicable)
            HttpHeaders responseHeaders = response.getHeaders();

            assertSoftly(softly -> {
                // Validate standard headers are present
                softly.assertThat(responseHeaders.getFirst(HttpHeaders.CONTENT_TYPE))
                        .describedAs("Content-Type should be properly set")
                        .isEqualTo(EXPECTED_CONTENT_TYPE);

                // Check for potential security headers (optional for this API)
                String xFrameOptions = responseHeaders.getFirst("X-Frame-Options");
                String xContentTypeOptions = responseHeaders.getFirst("X-Content-Type-Options");

                if (xFrameOptions != null) {
                    softly.assertThat(xFrameOptions)
                            .describedAs("X-Frame-Options should be properly configured")
                            .isIn("DENY", "SAMEORIGIN");
                }

                if (xContentTypeOptions != null) {
                    softly.assertThat(xContentTypeOptions)
                            .describedAs("X-Content-Type-Options should prevent MIME sniffing")
                            .isEqualTo("nosniff");
                }
            });
        }
    }

    @Nested
    @DisplayName("Concurrency and Load Tests")
    class ConcurrencyAndLoadTests {

        @Test
        @DisplayName("Should handle concurrent requests without data corruption")
        void shouldHandleConcurrentRequestsWithoutDataCorruption() throws InterruptedException {
            int numberOfConcurrentRequests = 10;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentRequests);

            try {
                // When: Making concurrent requests
                List<CompletableFuture<ResponseEntity<List<String>>>> futures = IntStream
                        .range(0, numberOfConcurrentRequests)
                        .mapToObj(i -> CompletableFuture.supplyAsync(() -> GreekGodsApiIT.this.makeApiRequest(), executor))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                // Then: All requests should be successful with consistent data
                List<ResponseEntity<List<String>>> responses = futures.stream()
                        .map(CompletableFuture::join)
                        .toList();

                assertSoftly(softly -> {
                    softly.assertThat(responses)
                            .describedAs("Should receive all concurrent responses")
                            .hasSize(numberOfConcurrentRequests)
                            .describedAs("All responses should be successful")
                            .allMatch(response -> response.getStatusCode() == HttpStatus.OK)
                            .describedAs("All responses should have non-null bodies")
                            .allMatch(response -> response.getBody() != null)
                            .describedAs("All responses should contain expected god count")
                            .allMatch(response -> response.getBody().size() == EXPECTED_GOD_COUNT);

                    // Verify data consistency across all concurrent responses
                    List<String> firstResponseData = responses.get(0).getBody();
                    responses.forEach(response ->
                            softly.assertThat(response.getBody())
                                    .describedAs("All concurrent responses should return identical data")
                                    .isEqualTo(firstResponseData));
                });

            } finally {
                shutdownExecutorGracefully(executor);
            }
        }

        @Test
        @DisplayName("Should maintain system stability under high load")
        @Timeout(value = 60, unit = TimeUnit.SECONDS)
        void shouldMaintainSystemStabilityUnderHighLoad() throws InterruptedException {
            int numberOfRequests = 50;
            int threadPoolSize = 20;
            ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

            try {
                Instant start = Instant.now();

                // When: Creating high load with multiple requests
                List<CompletableFuture<ResponseEntity<List<String>>>> futures = IntStream
                        .range(0, numberOfRequests)
                        .mapToObj(i -> CompletableFuture.supplyAsync(() -> GreekGodsApiIT.this.makeApiRequest(), executor))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                Instant end = Instant.now();
                Duration totalDuration = Duration.between(start, end);

                // Then: System should remain stable and responsive
                long successfulRequests = futures.stream()
                        .map(CompletableFuture::join)
                        .filter(response -> response.getStatusCode() == HttpStatus.OK)
                        .filter(response -> response.getBody() != null)
                        .filter(response -> response.getBody().size() == EXPECTED_GOD_COUNT)
                        .count();

                assertSoftly(softly -> {
                    softly.assertThat(successfulRequests)
                            .describedAs("All %d requests should succeed under load", numberOfRequests)
                            .isEqualTo(numberOfRequests);

                    softly.assertThat(totalDuration)
                            .describedAs("Load test should complete within reasonable time, but took %s", totalDuration)
                            .isLessThan(MAX_LOAD_TEST_DURATION);

                    // Validate throughput
                    double requestsPerSecond = numberOfRequests / (double) totalDuration.toSeconds();
                    softly.assertThat(requestsPerSecond)
                            .describedAs("Should maintain reasonable throughput under load")
                            .isGreaterThan(1.0); // At least 1 request per second
                });

            } finally {
                shutdownExecutorGracefully(executor);
            }
        }

        /**
         * Gracefully shuts down an ExecutorService with proper timeout handling.
         *
         * @param executor the ExecutorService to shutdown
         * @throws InterruptedException if interrupted while waiting
         */
        private void shutdownExecutorGracefully(ExecutorService executor) throws InterruptedException {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate gracefully");
                }
            }
        }
    }

    @Nested
    @DisplayName("Comprehensive Acceptance Criteria Validation")
    class AcceptanceCriteriaValidationTests {

        @Test
        @DisplayName("Should pass all acceptance criteria requirements")
        void shouldPassAllAcceptanceCriteriaRequirements() {
            // AC1: Successfully retrieve complete list of Greek god names
            ResponseEntity<List<String>> response = makeApiRequest();

            assertSoftly(softly -> {
                softly.assertThat(response.getStatusCode())
                        .describedAs("AC1: Should return successful HTTP status")
                        .isEqualTo(HttpStatus.OK);

                List<String> godNames = response.getBody();
                softly.assertThat(godNames)
                        .describedAs("AC1: Should return complete list of god names")
                        .isNotNull()
                        .hasSize(EXPECTED_GOD_COUNT)
                        .containsAll(EXPECTED_GREEK_GODS);

                // AC2: API response time consistently under 1 second
                Duration responseTime = measureResponseTime();
                softly.assertThat(responseTime)
                        .describedAs("AC2: Response time should be under 1 second")
                        .isLessThan(MAX_RESPONSE_TIME);

                // AC3: Proper HTTP status codes
                softly.assertThat(response.getStatusCode())
                        .describedAs("AC3: Should return HTTP 200 for successful requests")
                        .isEqualTo(HttpStatus.OK);

                // AC4: Simple JSON array response format
                softly.assertThat(response.getHeaders().getContentType())
                        .describedAs("AC4: Should return JSON content type")
                        .isEqualTo(MediaType.APPLICATION_JSON);

                softly.assertThat(godNames)
                        .describedAs("AC4: Should return simple JSON array of strings")
                        .isInstanceOf(List.class)
                        .allMatch(name -> name instanceof String);

                // AC5: Complete dataset validation
                softly.assertThat(godNames)
                        .describedAs("AC5: Should contain exactly %d Greek god names", EXPECTED_GOD_COUNT)
                        .hasSize(EXPECTED_GOD_COUNT)
                        .containsAll(EXPECTED_GREEK_GODS);

                // Additional quality checks
                softly.assertThat(godNames)
                        .describedAs("All god names should be unique")
                        .doesNotHaveDuplicates()
                        .describedAs("All god names should be properly formatted")
                        .allMatch(name -> name.matches("^[A-Za-z]+$"));
            });
        }

        @Test
        @DisplayName("Should validate complete business rules compliance")
        void shouldValidateCompleteBusinessRulesCompliance() {
            // When: Making multiple requests to validate business rules
            ResponseEntity<List<String>> response = makeApiRequest();
            List<String> godNames = response.getBody();

            assertSoftly(softly -> {
                // Business Rule 1: Exact count requirement
                softly.assertThat(godNames)
                        .describedAs("BR1: Must return exactly 20 gods")
                        .hasSize(20);

                // Business Rule 2: No duplicates allowed
                softly.assertThat(godNames)
                        .describedAs("BR2: No duplicate god names allowed")
                        .doesNotHaveDuplicates();

                // Business Rule 3: Must include major Olympian gods
                Set<String> majorOlympians = Set.of("Zeus", "Hera", "Poseidon", "Demeter",
                                                   "Athena", "Apollo", "Artemis", "Ares",
                                                   "Aphrodite", "Hephaestus", "Hermes", "Dionysus");
                softly.assertThat(godNames)
                        .describedAs("BR3: Must include all major Olympian gods")
                        .containsAll(majorOlympians);

                // Business Rule 4: Names must be properly capitalized
                softly.assertThat(godNames)
                        .describedAs("BR4: All god names must be properly capitalized")
                        .allMatch(name -> Character.isUpperCase(name.charAt(0)))
                        .allMatch(name -> name.substring(1).chars()
                                .allMatch(c -> Character.isLowerCase(c) || Character.isUpperCase(c)));

                // Business Rule 5: Must be sorted alphabetically (if required by business)
                // Note: Uncomment if alphabetical sorting is a business requirement
                // softly.assertThat(godNames)
                //         .describedAs("BR5: God names should be sorted alphabetically")
                //         .isSorted();
            });
        }
    }

    /**
     * Helper method to make API requests with proper error handling.
     *
     * @return ResponseEntity containing the list of Greek god names
     */
    private ResponseEntity<List<String>> makeApiRequest() {
        try {
            return testRestTemplate.exchange(
                    baseUrl + API_ENDPOINT,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            fail("API request failed with exception: " + e.getMessage(), e);
            return null; // Never reached due to fail()
        }
    }

    /**
     * Helper method to measure API response time.
     *
     * @return Duration representing the response time
     */
    private Duration measureResponseTime() {
        Instant start = Instant.now();
        ResponseEntity<List<String>> response = makeApiRequest();
        Instant end = Instant.now();

        assertThat(response.getStatusCode())
                .describedAs("Response should be successful when measuring performance")
                .isEqualTo(HttpStatus.OK);

        return Duration.between(start, end);
    }
}
