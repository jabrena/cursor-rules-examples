package info.jab.latency.solution;

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

@DisplayName("Greek Gods Literature Analyzer Acceptance Tests")
class GreekGodsLiteratureAnalyzerAcceptanceTest {

    private static final Logger logger = LoggerFactory.getLogger(GreekGodsLiteratureAnalyzerAcceptanceTest.class);

    private static final String SHAKESPEARE_API_PATH = "/shakespeare";
    private static final String GREEK_GODS_API_PATH = "/greek";

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    private GreekGodsLiteratureAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        logger.info("Starting test setup");

        // Configure the analyzer with WireMock server URLs
        String shakespeareApiUrl = wireMockServer.baseUrl() + SHAKESPEARE_API_PATH;
        String greekGodsApiUrl = wireMockServer.baseUrl() + GREEK_GODS_API_PATH;

        analyzer = new GreekGodsLiteratureAnalyzer(
            shakespeareApiUrl,
            greekGodsApiUrl,
            Duration.ofSeconds(10)
        );

        logger.debug("Created GreekGodsLiteratureAnalyzer with Shakespeare API: {} and Greek Gods API: {}",
                    shakespeareApiUrl, greekGodsApiUrl);

        // Setup mock responses
        setupMockResponses();

        logger.info("Test setup completed successfully");
    }

    @AfterEach
    void tearDown() {
        logger.debug("Starting test teardown");

        try {
            // Clean up resources if needed
            analyzer = null;
            logger.debug("Test resources cleaned up successfully");
        } catch (Exception e) {
            logger.warn("Error during test teardown", e);
        } finally {
            logger.info("Test teardown completed");
        }
    }

    private void setupMockResponses() {
        logger.debug("Setting up mock responses for external APIs");

        try {
            // Mock Shakespeare API response
            wireMockServer.stubFor(get(urlEqualTo(SHAKESPEARE_API_PATH))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("wiremock/shakespeare.json")));
            logger.trace("Shakespeare API mock response configured");

            // Mock Greek Gods API response
            wireMockServer.stubFor(get(urlEqualTo(GREEK_GODS_API_PATH))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("wiremock/greek-gods.json")));
            logger.trace("Greek Gods API mock response configured");

            logger.info("All mock responses configured successfully");

        } catch (Exception e) {
            logger.error("Failed to setup mock responses", e);
            throw new RuntimeException("Mock setup failed", e);
        }
    }

    @Test
    @DisplayName("Successfully analyze Shakespeare text for Greek god references")
    void shouldAnalyzeShakespeareForGreekGodReferences() {
        logger.info("Starting test: Successfully analyze Shakespeare text for Greek god references");

        try {
            logger.debug("Executing GreekGodsLiteratureAnalyzer.analyze()");

            List<String> result = analyzer.analyze();

            logger.debug("Analysis completed, found {} Greek god references", result.size());

            // Validate the results
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();

            // Log some of the found references for debugging
            if (!result.isEmpty()) {
                logger.debug("Sample Greek god references found: {}", result.subList(0, Math.min(3, result.size())));
            }

            logger.info("Test completed successfully - Greek god references found and validated");

        } catch (Exception e) {
            logger.error("Test failed during analysis execution", e);
            throw e;
        }
    }

    @Test
    @DisplayName("Handle empty or missing data gracefully")
    void shouldHandleEmptyDataGracefully() {
        logger.info("Starting test: Handle empty or missing data gracefully");

        try {
            // Override stubs with empty responses
            logger.debug("Setting up empty response stubs for error case testing");

            wireMockServer.stubFor(get(urlEqualTo(SHAKESPEARE_API_PATH))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"text\": \"\"}")));

            wireMockServer.stubFor(get(urlEqualTo(GREEK_GODS_API_PATH))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"gods\": []}")));

            logger.debug("Executing analysis with empty data");

            List<String> result = analyzer.analyze();

            logger.debug("Analysis with empty data completed, result size: {}", result.size());

            // Should handle empty data gracefully
            assertThat(result).isNotNull();
            // Result might be empty, which is acceptable for empty input

            logger.info("Test completed successfully - empty data handled gracefully");

        } catch (Exception e) {
            logger.error("Test failed during empty data handling", e);
            throw e;
        }
    }
}
