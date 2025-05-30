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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MythologyGodsEndpointIT {

    @LocalServerPort
    private int port;

    private WireMockServer greekApiMock;
    private WireMockServer romanApiMock;
    private WireMockServer nordicApiMock;
    private WireMockServer indianApiMock;
    private WireMockServer celtiberianApiMock;

    @BeforeEach
    void setUp() {
        // Configure REST Assured
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // Set up WireMock servers for each external mythology API
        setupMockServers();
        stubExternalMythologyAPIs();
    }

    @AfterEach
    void tearDown() {
        // Stop all WireMock servers
        stopMockServers();
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
                // And: The response time should be less than 5 seconds
                .time(lessThan(5000L))
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

    private void setupMockServers() {
        greekApiMock = new WireMockServer(WireMockConfiguration.options().port(8081));
        romanApiMock = new WireMockServer(WireMockConfiguration.options().port(8082));
        nordicApiMock = new WireMockServer(WireMockConfiguration.options().port(8083));
        indianApiMock = new WireMockServer(WireMockConfiguration.options().port(8084));
        celtiberianApiMock = new WireMockServer(WireMockConfiguration.options().port(8085));

        greekApiMock.start();
        romanApiMock.start();
        nordicApiMock.start();
        indianApiMock.start();
        celtiberianApiMock.start();
    }

    private void stopMockServers() {
        if (greekApiMock != null) greekApiMock.stop();
        if (romanApiMock != null) romanApiMock.stop();
        if (nordicApiMock != null) nordicApiMock.stop();
        if (indianApiMock != null) indianApiMock.stop();
        if (celtiberianApiMock != null) celtiberianApiMock.stop();
    }

    private void stubExternalMythologyAPIs() {
        // Stub Greek mythology API
        greekApiMock.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"Zeus\", \"Hera\", \"Poseidon\", \"Athena\"]")));

        // Stub Roman mythology API
        romanApiMock.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"Jupiter\", \"Juno\", \"Neptune\", \"Minerva\"]")));

        // Stub Nordic mythology API
        nordicApiMock.stubFor(get(urlEqualTo("/nordic"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"Odin\", \"Thor\", \"Frigg\", \"Loki\"]")));

        // Stub Indian mythology API
        indianApiMock.stubFor(get(urlEqualTo("/indian"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"Brahma\", \"Vishnu\", \"Shiva\", \"Lakshmi\"]")));

        // Stub Celtiberian mythology API
        celtiberianApiMock.stubFor(get(urlEqualTo("/celtiberian"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"Ataecina\", \"Endovelicus\", \"Bandua\", \"Reue\"]")));
    }
}
