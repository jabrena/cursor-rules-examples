package info.jab.latency;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import info.jab.latency.service.LatencyProblemSolver;

@DisplayName("Latency Problem Solver Acceptance Tests")
class LatencyProblemSolverAcceptanceIT {

    private static final Logger logger = LoggerFactory.getLogger(LatencyProblemSolverAcceptanceIT.class);

    private LatencyProblemSolver latencyProblemSolver;

    private static final String GREEK_API_PATH = "/greek/gods";
    private static final String ROMAN_API_PATH = "/roman/gods";
    private static final String NORDIC_API_PATH = "/nordic/gods";

    private static final BigInteger EXPECTED_ALL_GODS_SUM = new BigInteger("78179288397447443426");

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @BeforeEach
    void setUp() {
        logger.info("Starting test setup");

        List<String> apiUrls = List.of(
            wireMockServer.baseUrl() + GREEK_API_PATH,
            wireMockServer.baseUrl() + ROMAN_API_PATH,
            wireMockServer.baseUrl() + NORDIC_API_PATH
        );

        latencyProblemSolver = new LatencyProblemSolver(apiUrls, Duration.ofSeconds(5));

        logger.debug("Created LatencyProblemSolver with {} API URLs and timeout: {}",
                    apiUrls.size(), Duration.ofSeconds(5));
        logger.trace("API URLs configured: {}", apiUrls);

        logger.info("Test setup completed successfully");
    }

    @AfterEach
    void tearDown() {
        logger.debug("Starting test teardown");

        try {
            // Clean up resources if needed
            latencyProblemSolver = null;
            logger.debug("Test resources cleaned up successfully");
        } catch (Exception e) {
            logger.warn("Error during test teardown", e);
        } finally {
            logger.info("Test teardown completed");
        }
    }

    private String loadJsonFromFile(String filePath) throws IOException {
        logger.debug("Loading JSON from file: {}", filePath);

        try {
            String fullPath = "src/test/resources/__files/wiremock/" + filePath;
            String jsonContent = new String(Files.readAllBytes(Paths.get(fullPath)), StandardCharsets.UTF_8);

            logger.trace("Successfully loaded JSON file: {}, content length: {} characters",
                        filePath, jsonContent.length());

            return jsonContent;
        } catch (IOException e) {
            logger.error("Failed to load JSON from file: {}", filePath, e);
            throw e;
        }
    }

    private void stubGodProviderFromFile(String apiName, String endpointPath, String jsonFileName, int delayMillis, int statusCode) throws IOException {
        logger.debug("Setting up WireMock stub for: {}", apiName);

        try {
            String jsonBody = loadJsonFromFile(jsonFileName);
            wireMockServer.stubFor(get(urlEqualTo(endpointPath))
                    .willReturn(aResponse()
                            .withStatus(statusCode)
                            .withHeader("Content-Type", "application/json")
                            .withBody(jsonBody)
                            .withFixedDelay(delayMillis)));

            logger.info("Successfully stubbed {} API: endpoint={}, file={}, status={}, delay={}ms",
                       apiName, endpointPath, jsonFileName, statusCode, delayMillis);

        } catch (Exception e) {
            logger.error("Failed to setup WireMock stub for {}", apiName, e);
            throw e;
        }
    }

    @Test
    @DisplayName("Happy path: Consume all APIs, convert, and sum all gods")
    void consumeApisHappyPathAllGods() throws Exception {
        logger.info("Starting test: Happy path - consume all APIs and sum all gods");

        try {
            // Given
            logger.debug("Setting up WireMock stubs for all mythology APIs");
            stubGodProviderFromFile("Greek API", GREEK_API_PATH, "greek_gods.json", 0, 200);
            stubGodProviderFromFile("Roman API", ROMAN_API_PATH, "roman_gods.json", 0, 200);
            stubGodProviderFromFile("Nordic API", NORDIC_API_PATH, "nordic_gods.json", 0, 200);

            logger.info("All WireMock stubs configured successfully");

            // When
            logger.debug("Executing LatencyProblemSolver.solve()");
            BigInteger totalSum = latencyProblemSolver.solve();
            logger.info("LatencyProblemSolver completed successfully, result: {}", totalSum);

            // Then
            logger.debug("Validating result against expected sum: {}", EXPECTED_ALL_GODS_SUM);
            assertThat(totalSum).isEqualTo(EXPECTED_ALL_GODS_SUM);

            logger.info("Test completed successfully - all assertions passed");

        } catch (Exception e) {
            logger.error("Test failed during execution", e);
            throw e;
        }
    }

    @Test
    @DisplayName("E2E (Real APIs): Use LatencyProblemSolver with real APIs and assert the sum")
    void solve_withRealApis_shouldReturnExpectedSum() {
        logger.info("Starting test: E2E with real APIs");

        try {
            // Given
            final String greekApi = "https://my-json-server.typicode.com/jabrena/latency-problems/greek";
            final String romanApi = "https://my-json-server.typicode.com/jabrena/latency-problems/roman";
            final String nordicApi = "https://my-json-server.typicode.com/jabrena/latency-problems/nordic";
            final List<String> apis = List.of(greekApi, romanApi, nordicApi);
            final Duration timeout = Duration.ofSeconds(5);

            logger.debug("Configuring LatencyProblemSolver with real APIs");
            logger.trace("Real API endpoints: {}", apis);
            logger.debug("Timeout configured: {}", timeout);

            LatencyProblemSolver realLatencyProblemSolver = new LatencyProblemSolver(apis, timeout);

            logger.warn("Executing test with real external APIs - this may be slow or fail due to network issues");

            // When
            logger.debug("Executing LatencyProblemSolver.solve() with real APIs");
            BigInteger totalSum = realLatencyProblemSolver.solve();
            logger.info("Real API test completed successfully, result: {}", totalSum);

            // Then
            logger.debug("Validating result against expected sum: {}", EXPECTED_ALL_GODS_SUM);
            assertThat(totalSum).isEqualTo(EXPECTED_ALL_GODS_SUM);

            logger.info("E2E test completed successfully - all assertions passed");

        } catch (Exception e) {
            logger.error("E2E test failed - this could be due to network issues or API unavailability", e);
            throw e;
        }
    }
}
