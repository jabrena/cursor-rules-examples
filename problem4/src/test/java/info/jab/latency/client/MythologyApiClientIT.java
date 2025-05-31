package info.jab.latency.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import info.jab.latency.config.MythologyApiProperties;
import info.jab.latency.model.Mythology;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for MythologyApiClient to test all branches and exception scenarios.
 * Uses JSON files from test resources via WireMock's native withBodyFile method.
 */
@SpringBootTest
@ActiveProfiles("test")
class MythologyApiClientIT {

    @Autowired
    private RestClient restClient;

    private WireMockServer wireMockServer;
    private MythologyApiClient mythologyApiClient;
    private MythologyApiProperties properties;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8081));
        wireMockServer.start();

        properties = new MythologyApiProperties();
        properties.setBaseUrl("http://localhost:8081/");

        mythologyApiClient = new MythologyApiClient(restClient, properties);
    }

    @AfterEach
    void tearDown() {
        if (Objects.nonNull(wireMockServer)) {
            wireMockServer.stop();
        }
    }

    @Test
    void fetchGods_WhenSuccessfulResponse_ShouldReturnGodsList() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("greek-gods.json")));

        // When
        List<String> result = mythologyApiClient.fetchGods(Mythology.GREEK);

        // Then
        assertNotNull(result);
        assertEquals(20, result.size()); // The greek-gods.json file contains 20 gods
        assertTrue(result.contains("Zeus"));
        assertTrue(result.contains("Athena"));
        assertTrue(result.contains("Apollo"));
        assertTrue(result.contains("Poseidon"));
        assertTrue(result.contains("Hera"));
        assertTrue(result.contains("Hades"));
    }

    @Test
    void fetchGods_WhenEmptyResponse_ShouldReturnEmptyList() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        // When
        List<String> result = mythologyApiClient.fetchGods(Mythology.ROMAN);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchGods_WhenNullResponse_ShouldReturnEmptyList() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/nordic"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("null")));

        // When
        List<String> result = mythologyApiClient.fetchGods(Mythology.NORDIC);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchGods_When404NotFound_ShouldReturnEmptyList() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/indian"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Not Found\"}")));

        // When
        List<String> result = mythologyApiClient.fetchGods(Mythology.INDIAN);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchGods_When500ServerError_ShouldReturnEmptyList() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/celtiberian"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Internal Server Error\"}")));

        // When
        List<String> result = mythologyApiClient.fetchGods(Mythology.CELTIBERIAN);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchGods_WhenTimeout_ShouldReturnEmptyList() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withFixedDelay(6000) // Exceed the 5 second timeout
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("greek-gods.json")));

        // When
        List<String> result = mythologyApiClient.fetchGods(Mythology.GREEK);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchGods_WhenConnectionRefused_ShouldReturnEmptyList() {
        // Given - Stop the WireMock server to simulate connection refused
        wireMockServer.stop();

        // When
        List<String> result = mythologyApiClient.fetchGods(Mythology.ROMAN);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchGods_WhenInvalidJsonResponse_ShouldReturnEmptyList() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/nordic"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{ invalid json }")));

        // When
        List<String> result = mythologyApiClient.fetchGods(Mythology.NORDIC);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchGods_WhenWrongContentType_ShouldReturnEmptyList() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/indian"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody("Some plain text response")));

        // When
        List<String> result = mythologyApiClient.fetchGods(Mythology.INDIAN);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchGods_WhenConfigurationError_ShouldReturnEmptyList() {
        // Given - Create a client with invalid configuration that will cause IllegalStateException
        MythologyApiProperties invalidProperties = new MythologyApiProperties();
        invalidProperties.setBaseUrl(""); // Empty base URL

        MythologyApiClient clientWithInvalidConfig = new MythologyApiClient(restClient, invalidProperties);

        // When
        List<String> result = clientWithInvalidConfig.fetchGods(Mythology.GREEK);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchGods_WhenConnectionReset_ShouldReturnEmptyList() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/celtiberian"))
            .willReturn(aResponse()
                .withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)));

        // When
        List<String> result = mythologyApiClient.fetchGods(Mythology.CELTIBERIAN);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchGods_WhenEmptyResponseBody_ShouldReturnEmptyList() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("")));

        // When
        List<String> result = mythologyApiClient.fetchGods(Mythology.GREEK);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchGods_WhenMalformedUrl_ShouldReturnEmptyList() {
        // Given - Create properties with URL that will cause RestClientException (not during URL construction)
        MythologyApiProperties malformedProperties = new MythologyApiProperties();
        malformedProperties.setBaseUrl("http://invalid-host-that-does-not-exist.test/");

        MythologyApiClient clientWithMalformedUrl = new MythologyApiClient(restClient, malformedProperties);

        // When
        List<String> result = clientWithMalformedUrl.fetchGods(Mythology.ROMAN);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
