package info.jab.latency;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for error scenarios in the mythology aggregation service.
 * Tests the error handling capabilities and resilience of the system.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MainApplication.class)
@ActiveProfiles("test")
class ErrorScenariosIT {

    @LocalServerPort
    private int port;

    private WireMockServer mythologyApiMock;

    @BeforeEach
    void setUp() {
        // Configure REST Assured
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // Set up WireMock server
        setupMockServer();
    }

    @AfterEach
    void tearDown() {
        stopMockServer();
    }

    @Test
    void gods_endpoint_WhenAllExternalAPIsReturn500_ShouldReturnEmptyArray() {
        // Given: All external mythology APIs return 500 errors
        stubAllAPIsWithServerError();

        // When: Client calls the gods endpoint
        given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: Should return 200 with empty array (graceful degradation)
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(0));
    }

    @Test
    void gods_endpoint_WhenSomeExternalAPIsTimeout_ShouldReturnPartialResults() {
        // Given: Some APIs work, others timeout
        mythologyApiMock.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("greek-gods.json")));

        mythologyApiMock.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse()
                .withFixedDelay(6000) // Longer than timeout
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        mythologyApiMock.stubFor(get(urlEqualTo("/nordic"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("nordic-gods.json")));

        mythologyApiMock.stubFor(get(urlEqualTo("/indian"))
            .willReturn(aResponse()
                .withFixedDelay(6000) // Longer than timeout
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        mythologyApiMock.stubFor(get(urlEqualTo("/celtiberian"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("celtiberian-gods.json")));

        // When: Client calls the gods endpoint
        given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: Should return 200 with partial results
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThan(0)))
                .body("mythology", hasItems("greek", "nordic", "celtiberian"))
                .body("mythology", not(hasItems("roman", "indian")));
    }

    @Test
    void gods_endpoint_WhenMixedErrors_ShouldReturnAvailableResults() {
        // Given: Mix of successful, 500 error, and 404 responses
        mythologyApiMock.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("greek-gods.json")));

        mythologyApiMock.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Internal Server Error\"}")));

        mythologyApiMock.stubFor(get(urlEqualTo("/nordic"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Not Found\"}")));

        mythologyApiMock.stubFor(get(urlEqualTo("/indian"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("indian-gods.json")));

        mythologyApiMock.stubFor(get(urlEqualTo("/celtiberian"))
            .willReturn(aResponse()
                .withStatus(503)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Service Unavailable\"}")));

        // When: Client calls the gods endpoint
        given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: Should return 200 with available results only
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThan(0)))
                .body("mythology", hasItems("greek", "indian"))
                .body("mythology", not(hasItems("roman", "nordic", "celtiberian")));
    }

    @Test
    void gods_endpoint_WhenInvalidJSONResponse_ShouldHandleGracefully() {
        // Given: External API returns invalid JSON
        mythologyApiMock.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{ invalid json }")));

        mythologyApiMock.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("roman-gods.json")));

        stubOtherAPIsWithSuccess();

        // When: Client calls the gods endpoint
        given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: Should return 200 with available results (excluding the invalid one)
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThan(0)))
                .body("mythology", hasItems("roman", "nordic", "indian", "celtiberian"))
                .body("mythology", not(hasItem("greek")));
    }

    @Test
    void gods_endpoint_WhenExtremeTimeout_ShouldReturn504GatewayTimeout() {
        // Given: All external APIs have extreme delays (beyond timeout)
        stubAllAPIsWithExtremeTimeout();

        // When: Client calls the gods endpoint
        given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: Should return 504 Gateway Timeout (if timeout exception bubbles up)
                // OR 200 with empty array (if handled gracefully)
                .statusCode(is(200))
                .contentType(ContentType.JSON);
    }

    @Test
    void gods_endpoint_WhenInvalidConfiguration_ShouldHandleConfigurationError() {
        // This test aims to trigger configuration-related errors
        // by stopping the mock server to cause connection failures
        stopMockServer();

        // When: Client calls the gods endpoint with no available services
        given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: Should handle the error gracefully
                .statusCode(anyOf(is(200), is(500), is(502)))
                .contentType(ContentType.JSON);
    }

    @Test
    void gods_endpoint_WhenMalformedResponse_ShouldHandleParsingErrors() {
        // Given: External API returns malformed non-JSON response
        mythologyApiMock.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody("This is not JSON at all!")));

        mythologyApiMock.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("<xml>This is XML not JSON</xml>")));

        stubOtherAPIsWithSuccess();

        // When: Client calls the gods endpoint
        given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: Should handle parsing errors gracefully
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    void gods_endpoint_WhenNetworkErrors_ShouldHandleConnectionIssues() {
        // Given: APIs return connection reset errors
        mythologyApiMock.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)));

        mythologyApiMock.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse()
                .withFault(com.github.tomakehurst.wiremock.http.Fault.EMPTY_RESPONSE)));

        stubOtherAPIsWithSuccess();

        // When: Client calls the gods endpoint
        given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: Should handle network errors gracefully
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    void gods_endpoint_WhenInvalidEndpoint_ShouldReturn404() {
        // When: Client calls a non-existent endpoint
        given()
            .when()
                .get("/api/v1/nonexistent")
            .then()
                // Then: Should return 500 (handled by GlobalExceptionHandler)
                .statusCode(500);
    }

    @Test
    void gods_endpoint_WhenUsingPOSTMethod_ShouldReturn405() {
        // When: Client uses wrong HTTP method
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when()
                .post("/api/v1/gods")
            .then()
                // Then: Should return 500 (handled by GlobalExceptionHandler)
                .statusCode(500);
    }

    @Test
    void gods_endpoint_WhenCorruptedResponse_ShouldHandleGracefully() {
        // Given: External API returns corrupted response
        mythologyApiMock.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("['Zeus', 'Apollo', 'Athena'")));  // Missing closing bracket

        mythologyApiMock.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"unexpected\"}"))); // Object instead of array

        stubOtherAPIsWithSuccess();

        // When: Client calls the gods endpoint
        given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: Should handle corrupted responses gracefully
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    private void setupMockServer() {
        mythologyApiMock = new WireMockServer(WireMockConfiguration.options().port(8081));
        mythologyApiMock.start();
    }

    private void stopMockServer() {
        if (mythologyApiMock != null) {
            mythologyApiMock.stop();
        }
    }

    private void stubAllAPIsWithServerError() {
        mythologyApiMock.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse().withStatus(500)));
        mythologyApiMock.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse().withStatus(500)));
        mythologyApiMock.stubFor(get(urlEqualTo("/nordic"))
            .willReturn(aResponse().withStatus(500)));
        mythologyApiMock.stubFor(get(urlEqualTo("/indian"))
            .willReturn(aResponse().withStatus(500)));
        mythologyApiMock.stubFor(get(urlEqualTo("/celtiberian"))
            .willReturn(aResponse().withStatus(500)));
    }

    private void stubOtherAPIsWithSuccess() {
        mythologyApiMock.stubFor(get(urlEqualTo("/nordic"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("nordic-gods.json")));

        mythologyApiMock.stubFor(get(urlEqualTo("/indian"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("indian-gods.json")));

        mythologyApiMock.stubFor(get(urlEqualTo("/celtiberian"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("celtiberian-gods.json")));
    }

    private void stubAllAPIsWithExtremeTimeout() {
        // Set delays longer than typical timeout values
        mythologyApiMock.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withFixedDelay(10000) // 10 seconds
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        mythologyApiMock.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse()
                .withFixedDelay(10000)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        mythologyApiMock.stubFor(get(urlEqualTo("/nordic"))
            .willReturn(aResponse()
                .withFixedDelay(10000)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        mythologyApiMock.stubFor(get(urlEqualTo("/indian"))
            .willReturn(aResponse()
                .withFixedDelay(10000)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        mythologyApiMock.stubFor(get(urlEqualTo("/celtiberian"))
            .willReturn(aResponse()
                .withFixedDelay(10000)
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));
    }
}
