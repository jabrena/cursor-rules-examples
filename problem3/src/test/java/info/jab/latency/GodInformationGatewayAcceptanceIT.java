package info.jab.latency;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;

/**
 * Acceptance Test for God Information Gateway Service
 *
 * Epic: EPIC-001 - God Information Gateway API
 * Feature: FEAT-001 - God Information Gateway API
 * User Story: US-001 - Basic God Information Service
 *
 * This test uses WireMock to simulate external mythology API responses
 * based on the examples from the OpenAPI specification (gateway-api.yaml).
 * The test verifies the acceptance criteria from the Gherkin feature file:
 * docs/problem3/agile/basic_god_information_gateway_service.feature
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GodInformationGatewayAcceptanceIT {

    private static final Logger logger = LoggerFactory.getLogger(GodInformationGatewayAcceptanceIT.class);

    @LocalServerPort
    private int port;

    private static final WireMockServer wireMockServer;

    static {
        // Initialize WireMock server in static block to ensure it's available during Spring context startup
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        logger.info("WireMock server started on port: {}", wireMockServer.port());

        // Setup shutdown hook to clean up properly
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (wireMockServer.isRunning()) {
                wireMockServer.stop();
                logger.info("WireMock server stopped during JVM shutdown");
            }
        }));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (!wireMockServer.isRunning()) {
            logger.error("WireMock server is not running during property configuration");
            throw new IllegalStateException("WireMock server is not running during property configuration");
        }

        String mythologyApiBaseUrl = "http://localhost:" + wireMockServer.port();
        registry.add("mythology.api.base-url", () -> mythologyApiBaseUrl);
        registry.add("logging.level.info.jab.latency", () -> "DEBUG");

        logger.info("Configured mythology API base URL: {}", mythologyApiBaseUrl);
    }

    @BeforeEach
    void setup() {
        logger.info("Starting test setup");

        // Verify server is still running and reset stubs for each test
        if (!wireMockServer.isRunning()) {
            logger.error("WireMock server is not running during test setup");
            throw new IllegalStateException("WireMock server is not running during test setup");
        }

        wireMockServer.resetAll();
        logger.debug("WireMock server reset completed");

        setupMockResponses();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        logger.info("Test setup completed successfully");
    }

    @AfterEach
    void tearDown() {
        logger.debug("Starting test teardown");

        // Don't stop the server, just reset it for the next test
        if (wireMockServer.isRunning()) {
            wireMockServer.resetAll();
            logger.debug("WireMock server reset for next test");
        } else {
            logger.warn("WireMock server was not running during teardown");
        }

        logger.info("Test teardown completed");
    }

    private void setupMockResponses() {
        logger.debug("Setting up mock responses for external mythology APIs");

        try {
            // Mock Greek gods response using WireMock's native file approach
            wireMockServer.stubFor(get(urlEqualTo("/greek"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("wiremock/greek-gods.json")));
            logger.trace("Greek gods mock response configured");

            logger.info("All mock responses configured successfully");
        } catch (Exception e) {
            logger.error("Failed to setup mock responses", e);
            throw new RuntimeException("Mock setup failed", e);
        }
    }

    @Test
    @DisplayName("Successfully retrieve Greek gods list")
    void shouldRetrieveGreekGodsList() {
        logger.info("Starting test: Successfully retrieve Greek gods list");

        // This test implements the acceptance criteria from:
        // Background: Given the God Information Gateway API is running on localhost:8080
        // And all external mythology APIs are available (mocked with WireMock)
        //
        // Scenario: Successfully retrieve Greek gods list
        //   Given all external mythology APIs are available
        //   When I make a GET request to "/api/v1/gods/greek"
        //   Then I should receive HTTP 200 status
        //   And the response should be in JSON format
        //   And the response should contain mythology: "greek", gods: ["Zeus", "Hera", "Poseidon"], count: 20, source: "external_api", timestamp: current timestamp
        //   And the response should include gods "Zeus", "Hera", "Poseidon"
        logger.debug("Making GET request to: /api/v1/gods/greek");

        given()
            .contentType("application/json")
            .log().all()
        .when()
            .get("/api/v1/gods/greek")
        .then()
            .log().all()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("mythology", equalTo("greek"))
            .body("gods", hasItems("Zeus", "Hera", "Poseidon"))
            .body("count", equalTo(20))
            .body("source", equalTo("external_api"))
            .body("timestamp", notNullValue())
            .body("gods", hasSize(greaterThan(0)));
    }
}
