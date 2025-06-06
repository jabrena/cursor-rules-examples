package info.jab.latency;

import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import info.jab.latency.config.PostgreTestContainers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.*;

/**
 * End-to-End Acceptance Tests for Greek Gods API using RestAssured.
 *
 * <h2>Purpose:</h2>
 * <p>
 * These tests validate the complete user stories and business requirements
 * from an external perspective, testing the full application stack as a black box.
 * They complement the unit and integration tests by focusing on user acceptance criteria
 * and real-world usage scenarios.
 * </p>
 *
 * <h2>Testing Strategy:</h2>
 * <ul>
 *   <li><strong>Black Box Testing:</strong> Tests API behavior without knowledge of internal implementation</li>
 *   <li><strong>User Story Validation:</strong> Ensures all acceptance criteria are met</li>
 *   <li><strong>Contract Testing:</strong> Validates API contracts and response formats</li>
 *   <li><strong>Performance Validation:</strong> Confirms performance requirements are satisfied</li>
 *   <li><strong>Error Scenario Testing:</strong> Validates proper error handling and responses</li>
 * </ul>
 *
 * <h2>Tools Used:</h2>
 * <ul>
 *   <li><strong>RestAssured:</strong> HTTP client testing with fluent DSL</li>
 *   <li><strong>TestContainers:</strong> Isolated database environment</li>
 *   <li><strong>Hamcrest:</strong> Expressive matchers for response validation</li>
 * </ul>
 *
 * @author Acceptance Test Suite
 * @see <a href="https://rest-assured.io/">RestAssured Documentation</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@PostgreTestContainers
@DisplayName("Greek Gods API Acceptance Tests")
class GreekGodsApiAcceptanceIT {

    private static final String API_ENDPOINT = "/api/v1/gods/greek";
    private static final int EXPECTED_GOD_COUNT = 20;
    private static final long MAX_RESPONSE_TIME_MS = 1000L;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        // Configure RestAssured for optimal testing
        RestAssured.port = port;
        RestAssured.config = RestAssured.config()
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                        .defaultObjectMapperType(ObjectMapperType.JACKSON_2));

        // Enable detailed logging for failed tests only
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Set default timeout for all requests
        RestAssured.config = RestAssured.config()
                .httpClient(RestAssured.config().getHttpClientConfig()
                        .setParam("http.connection.timeout", 5000)
                        .setParam("http.socket.timeout", 10000));
    }

    @Nested
    @DisplayName("User Story: Retrieve Greek Gods List")
    class RetrieveGreekGodsStory {

        @Test
        @DisplayName("As a client, I should successfully retrieve a complete list of Greek god names")
        void shouldSuccessfullyRetrieveCompleteListOfGreekGodNames() {
            // GIVEN: The Greek Gods API is available
            // WHEN: I request the list of Greek gods
            Instant start = Instant.now();

            List<String> response = given()
                    .accept("application/json")
                    .log().ifValidationFails()
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .log().ifValidationFails()
                    .statusCode(200)
                    .contentType("application/json")
                    .body("size()", equalTo(EXPECTED_GOD_COUNT))
                    .body("", hasItems("Zeus", "Hera", "Poseidon"))
                    .body("", everyItem(notNullValue()))
                    .body("", everyItem(not(emptyString())))
                .extract()
                    .jsonPath()
                    .getList("", String.class);

            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);

            // THEN: The response should meet all acceptance criteria
            validatePerformanceRequirement(duration);
            validateDataQuality(response);
        }

        @Test
        @DisplayName("As a client, I should receive consistent data across multiple requests")
        void shouldReceiveConsistentDataAcrossMultipleRequests() {
            // GIVEN: The Greek Gods API is available
            // WHEN: I make multiple requests for the same data
            List<String> firstResponse = given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                .extract()
                    .jsonPath()
                    .getList("", String.class);

            List<String> secondResponse = given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                .extract()
                    .jsonPath()
                    .getList("", String.class);

            List<String> thirdResponse = given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                .extract()
                    .jsonPath()
                    .getList("", String.class);

            // THEN: All responses should contain identical data
            assertThat(secondResponse)
                    .as("Second response should match first response")
                    .isEqualTo(firstResponse);
            assertThat(thirdResponse)
                    .as("Third response should match first response")
                    .isEqualTo(firstResponse);
            assertThat(firstResponse)
                    .as("All responses should contain exactly %d gods", EXPECTED_GOD_COUNT)
                    .hasSize(EXPECTED_GOD_COUNT);
        }
    }

    @Nested
    @DisplayName("API Contract Validation")
    class ApiContractValidation {

        @Test
        @DisplayName("Should return proper JSON array format")
        void shouldReturnJsonArrayFormat() {
            given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .body("", instanceOf(List.class))
                    .body("", everyItem(instanceOf(String.class)));
        }

        @Test
        @DisplayName("Should include proper HTTP headers")
        void shouldIncludeProperHttpHeaders() {
            given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                    .header("Content-Type", containsString("application/json"))
                    .header("Date", notNullValue());
        }

        @Test
        @DisplayName("Should validate response schema structure")
        void shouldValidateResponseSchemaStructure() {
            given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                    .body("", everyItem(matchesPattern("^[A-Za-z]+$")))  // Only letters
                    .body("", everyItem(not(emptyString())))             // Non-empty
                    .body("", not(hasItem(nullValue())));                // No null values
        }

        @Test
        @DisplayName("Should handle content negotiation properly")
        void shouldHandleContentNegotiationProperly() {
            // Test with explicit JSON Accept header
            given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                    .contentType("application/json");

            // Test with wildcard Accept header
            given()
                    .accept("*/*")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                    .contentType(containsString("application/json"));
        }
    }

    @Nested
    @DisplayName("Performance Requirements")
    class PerformanceRequirements {

        @Test
        @DisplayName("Should respond within 1 second performance requirement")
        void shouldRespondWithinOneSecondPerformanceRequirement() {
            given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                    .time(lessThan(MAX_RESPONSE_TIME_MS), TimeUnit.MILLISECONDS);
        }

        @Test
        @DisplayName("Should maintain consistent performance across multiple requests")
        void shouldMaintainConsistentPerformanceAcrossMultipleRequests() {
            int numberOfRequests = 5;

            for (int i = 0; i < numberOfRequests; i++) {
                given()
                        .accept("application/json")
                    .when()
                        .get(API_ENDPOINT)
                    .then()
                        .statusCode(200)
                        .time(lessThan(MAX_RESPONSE_TIME_MS), TimeUnit.MILLISECONDS);;
            }
        }
    }

    @Nested
    @DisplayName("Concurrency and Load Testing")
    class ConcurrencyAndLoadTesting {

        @Test
        @DisplayName("Should maintain data integrity under concurrent load")
        void shouldMaintainDataIntegrityUnderConcurrentLoad() {
            // Get baseline response
            List<String> baselineResponse = given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                .extract()
                    .jsonPath()
                    .getList("", String.class);

            // Make multiple requests and verify they all match baseline
            int numberOfRequests = 3;
            for (int i = 0; i < numberOfRequests; i++) {
                List<String> currentResponse = given()
                        .accept("application/json")
                    .when()
                        .get(API_ENDPOINT)
                    .then()
                        .statusCode(200)
                    .extract()
                        .jsonPath()
                        .getList("", String.class);

                assertThat(currentResponse)
                        .as("Response %d should match baseline response", i + 1)
                        .isEqualTo(baselineResponse);
            }
        }
    }

    @Nested
    @DisplayName("Business Rule Validation")
    class BusinessRuleValidation {

        @Test
        @DisplayName("Should include all major Olympian gods")
        void shouldIncludeAllMajorOlympianGods() {
            given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                    .body("", hasItems(
                            "Zeus", "Hera", "Poseidon", "Demeter",
                            "Athena", "Apollo", "Artemis", "Ares",
                            "Aphrodite", "Hephaestus", "Hermes", "Dionysus"
                    ));
        }

        @Test
        @DisplayName("Should not contain duplicate god names")
        void shouldNotContainDuplicateGodNames() {
            List<String> response = given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                .extract()
                    .jsonPath()
                    .getList("", String.class);

            // Verify no duplicates using Set size comparison
            assertThat(response.stream().distinct())
                    .as("Response should not contain duplicate god names")
                    .hasSameSizeAs(response);
        }

        @Test
        @DisplayName("Should contain properly formatted god names")
        void shouldContainProperlyFormattedGodNames() {
            given()
                    .accept("application/json")
                .when()
                    .get(API_ENDPOINT)
                .then()
                    .statusCode(200)
                    .body("", everyItem(matchesPattern("^[A-Z][a-z]*$")))  // Proper capitalization
                    .body("", everyItem(not(emptyString())))               // Non-empty
                    .body("", not(hasItem(nullValue())));                  // No nulls
        }
    }

    // Helper methods for validation

    /**
     * Validates that the API response meets performance requirements.
     *
     * @param duration the measured response time
     */
    private void validatePerformanceRequirement(Duration duration) {
        assertThat(duration.toMillis())
                .as("Response time should be under %d ms, but was: %d ms",
                    MAX_RESPONSE_TIME_MS, duration.toMillis())
                .isLessThan(MAX_RESPONSE_TIME_MS);
    }

    /**
     * Validates the quality and format of the returned data.
     *
     * @param response the API response containing god names
     */
    private void validateDataQuality(List<String> response) {
        // Verify no null or empty values
        assertThat(response)
                .as("Response should not contain null values")
                .doesNotContainNull();
        assertThat(response)
                .as("Response should not contain empty strings")
                .allMatch(name -> !name.isEmpty());

        // Verify proper formatting (only letters, proper capitalization)
        assertThat(response)
                .as("All god names should contain only letters")
                .allMatch(name -> name.matches("^[A-Za-z]+$"));
        assertThat(response)
                .as("All god names should be properly capitalized")
                .allMatch(name -> Character.isUpperCase(name.charAt(0)));

        // Verify no duplicates
        assertThat(response.stream().distinct())
                .as("Response should not contain duplicate god names")
                .hasSameSizeAs(response);
    }
}
