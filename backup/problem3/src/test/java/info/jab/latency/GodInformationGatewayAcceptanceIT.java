package info.jab.latency;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;

/**
 * Acceptance Test for God Information Gateway Service
 *
 * Epic: EPIC-001 - God Information Gateway API
 * Feature: FEAT-001 - God Information Gateway API
 * User Story: US-001 - Basic God Information Service
 *
 * Scenario: Successfully retrieve Greek gods list
 *
 * This test is expected to FAIL because the actual service is not implemented yet.
 * The test simulates the acceptance criteria from the Gherkin feature file:
 * docs/problem3/agile/basic_god_information_gateway_service.feature
 */
 class GodInformationGatewayAcceptanceIT {

    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.port = 8080;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("Successfully retrieve Greek gods list - EXPECTED TO FAIL")
    void shouldRetrieveGreekGodsList() {
        // This test implements the acceptance criteria from:
        // Background: Given the God Information Gateway API is running on localhost:8080
        // And all external mythology APIs are available at "https://my-json-server.typicode.com/jabrena/latency-problems/"
        //
        // Scenario: Successfully retrieve Greek gods list
        //   Given all external mythology APIs are available
        //   When I make a GET request to "/api/v1/gods/GREEK"
        //   Then I should receive HTTP 200 status
        //   And the response should be in JSON format
        //   And the response should contain mythology: "GREEK", gods: ["Zeus", "Hera", "Poseidon"], count: 20, source: "external_api", timestamp: current timestamp
        //   And the response should include gods "Zeus", "Hera", "Poseidon"
        given()
            .contentType("application/json")
            .log().all()
        .when()
            .get("/api/v1/gods/GREEK")
        .then()
            .log().all()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("mythology", equalTo("GREEK"))
            .body("gods", hasItems("Zeus", "Hera", "Poseidon"))
            .body("count", equalTo(20))
            .body("source", equalTo("external_api"))
            .body("timestamp", notNullValue())
            .body("gods", hasSize(greaterThan(0)));
    }

    @Test
    @DisplayName("API should handle invalid mythology parameter - EXPECTED TO FAIL")
    public void shouldHandleInvalidMythologyParameter() {
        // Test error handling for invalid mythology types
        given()
            .contentType("application/json")
            .log().all()
        .when()
            .get("/api/v1/gods/INVALID")
        .then()
            .log().all()
            .statusCode(400)
            .body(anyOf(
                containsString("INVALID"),
                containsString("error"),
                containsString("Bad Request")
            ));
    }

    @Test
    @DisplayName("API should handle Roman gods request - EXPECTED TO FAIL")
    public void shouldRetrieveRomanGodsList() {
        // Additional test case for Roman mythology
        try {
            given()
                .contentType("application/json")
                .log().all()
            .when()
                .get("/api/v1/gods/ROMAN")
            .then()
                .log().all()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("mythology", equalTo("ROMAN"))
                .body("gods", hasItems("Jupiter", "Mars", "Venus"))
                .body("count", equalTo(20))
                .body("source", equalTo("external_api"))
                .body("timestamp", notNullValue());

        } catch (Exception e) {
            // This is expected to fail since no service is implemented
            fail("Roman gods test failed as expected - God Information Gateway service is not running: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("API should handle Norse gods request - EXPECTED TO FAIL")
    public void shouldRetrieveNorseGodsList() {
        // Additional test case for Norse mythology
        try {
            given()
                .contentType("application/json")
                .log().all()
            .when()
                .get("/api/v1/gods/NORSE")
            .then()
                .log().all()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("mythology", equalTo("NORSE"))
                .body("gods", hasItems("Odin", "Thor", "Freya"))
                .body("count", equalTo(20))
                .body("source", equalTo("external_api"))
                .body("timestamp", notNullValue());

        } catch (Exception e) {
            // This is expected to fail since no service is implemented
            fail("Norse gods test failed as expected - God Information Gateway service is not running: " + e.getMessage());
        }
    }
}
