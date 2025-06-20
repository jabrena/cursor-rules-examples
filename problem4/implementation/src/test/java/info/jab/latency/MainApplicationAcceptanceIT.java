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

import java.util.Objects;

/**
 * Acceptance test for the mythology gods endpoint.
 *
 * This test implements the acceptance criteria from the Gherkin scenario:
 * "Successfully retrieve all mythology gods data"
 *
 * Based on ADR-001 Acceptance Testing Strategy, this test uses:
 * - REST Assured for API testing
 * - WireMock for stubbing external mythology APIs
 * - Static test data based on OpenAPI specification examples
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MainApplication.class)
@ActiveProfiles("test")
class MainApplicationAcceptanceIT {

    @LocalServerPort
    private int port;

    private WireMockServer mythologyApiMock;

    @BeforeEach
    void setUp() {
        // Configure REST Assured
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // Set up single WireMock server for all external mythology API endpoints
        setupMockServer();
        stubExternalMythologyAPIs();
    }

    @AfterEach
    void tearDown() {
        // Stop WireMock server
        stopMockServer();
    }

    @Test
    void successfully_retrieve_all_mythology_gods_data() {
        // Given: The mythology aggregation service is running
        // And: All external mythology APIs are available (mocked)
        // And: The client application is ready to make API requests

        // When: The client sends a GET request to "/api/v1/gods"
        given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: The response status should be 200
                .statusCode(200)
                // And: The response content type should be "application/json"
                .contentType(ContentType.JSON)
                // And: The response time should be less than 2.5 seconds
                .time(lessThan(2500L))
                // And: The response should contain JSON formatted mythology data
                .body("$", notNullValue())
                .body("size()", greaterThan(0))
                // And: Each god entry should contain required fields
                .body("id", everyItem(notNullValue()))
                .body("mythology", everyItem(notNullValue()))
                .body("god", everyItem(notNullValue()))
                // And: The response should include gods from all mythologies
                .body("mythology", hasItems("greek", "roman", "nordic", "indian", "celtiberian"))
                // And: The response should have the expected structure with specific gods
                .body("findAll { it.mythology == 'greek' }.god", hasItem("Zeus"))
                .body("findAll { it.mythology == 'roman' }.god", hasItem("Jupiter"))
                .body("findAll { it.mythology == 'nordic' }.god", hasItem("Odin"))
                .body("findAll { it.mythology == 'indian' }.god", hasItem("Brahma"))
                .body("findAll { it.mythology == 'celtiberian' }.god", hasItem("Ataecina"))
                // Additional validation: Verify the complete structure matches expected format
                .body("$", hasSize(greaterThanOrEqualTo(5)));
    }

    private void setupMockServer() {
        // Create single WireMock server for all mythology API endpoints on port 8081
        mythologyApiMock = new WireMockServer(WireMockConfiguration.options().port(8081));
        mythologyApiMock.start();
    }

    private void stopMockServer() {
        if (Objects.nonNull(mythologyApiMock)) {
            mythologyApiMock.stop();
        }
    }

    private void stubExternalMythologyAPIs() {
        // Stub Greek mythology API endpoint (/greek)
        mythologyApiMock.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("greek-gods.json")));

        // Stub Roman mythology API endpoint (/roman)
        mythologyApiMock.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("roman-gods.json")));

        // Stub Nordic mythology API endpoint (/nordic)
        mythologyApiMock.stubFor(get(urlEqualTo("/nordic"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("nordic-gods.json")));

        // Stub Indian mythology API endpoint (/indian)
        mythologyApiMock.stubFor(get(urlEqualTo("/indian"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("indian-gods.json")));

        // Stub Celtiberian mythology API endpoint (/celtiberian)
        mythologyApiMock.stubFor(get(urlEqualTo("/celtiberian"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("celtiberian-gods.json")));
    }
}

