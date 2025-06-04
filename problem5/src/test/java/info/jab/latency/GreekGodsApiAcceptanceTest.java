package info.jab.latency;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Acceptance tests for Greek Gods API endpoints.
 *
 * This class implements the acceptance criteria defined in the Gherkin feature file:
 * US-001_api_greek_gods_data_retrieval.feature
 *
 * Following Outside-in TDD approach - these tests will initially fail until
 * the implementation is developed.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Greek Gods API Acceptance Tests")
class GreekGodsApiAcceptanceTest {

    @LocalServerPort
    private int port;

    private static final String GREEK_GODS_ENDPOINT = "/api/v1/gods/greek";

    /**
     * Expected list of 20 Greek god names as defined in the acceptance criteria
     */
    private static final List<String> EXPECTED_GREEK_GODS = Arrays.asList(
        "Zeus", "Hera", "Poseidon", "Demeter", "Ares", "Athena", "Apollo",
        "Artemis", "Hephaestus", "Aphrodite", "Hermes", "Dionysus", "Hades",
        "Hypnos", "Nike", "Janus", "Nemesis", "Iris", "Hecate", "Tyche"
    );

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /**
     * Acceptance Test for Scenario: Successfully retrieve complete list of Greek god names
     *
     * Given the database contains all 20 Greek god records
     * When I send a GET request to "/api/v1/gods/greek"
     * Then I should receive a 200 OK response
     * And the response should be a JSON array
     * And the response should contain exactly 20 Greek god names
     * And the response should include all expected god names
     * And each god name should be a string value
     * And there should be no duplicate entries in the response
     */
    @Test
    @DisplayName("Should successfully retrieve complete list of Greek god names")
    void shouldSuccessfullyRetrieveCompleteListOfGreekGodNames() {
        // Given: the database contains all 20 Greek god records
        // (This will be set up when the implementation is created)

        // When: I send a GET request to "/api/v1/gods/greek"
        // Then: I should receive a 200 OK response
        // And: the response should be a JSON array
        given()
            .contentType(ContentType.JSON)
        .when()
            .get(GREEK_GODS_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", instanceOf(List.class))
            // And: the response should contain exactly 20 Greek god names
            .body("size()", equalTo(20))
            // And: the response should include all expected god names
            .body("$", containsInAnyOrder(EXPECTED_GREEK_GODS.toArray()))
            // And: each god name should be a string value
            .body("every { it instanceof String }", is(true))
            // And: there should be no duplicate entries in the response
            .body("unique()", hasSize(20));
    }

    /**
     * Acceptance Test for Performance Requirement
     *
     * Given the database contains Greek god data
     * When I send a GET request to "/api/v1/gods/greek"
     * Then I should receive a response within 1 second
     * And the response status should be 200 OK
     */
    @Test
    @DisplayName("Should meet performance requirements - response within 1 second")
    void shouldMeetPerformanceRequirements() {
        long startTime = System.currentTimeMillis();

        given()
            .contentType(ContentType.JSON)
        .when()
            .get(GREEK_GODS_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);

        long responseTime = System.currentTimeMillis() - startTime;

        // Verify response time is under 1 second (1000ms)
        if (responseTime >= 1000) {
            throw new AssertionError(
                String.format("Response time %dms exceeded 1 second requirement", responseTime)
            );
        }
    }
}
