package info.jab.latency;

import java.math.BigInteger;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

class LatencyProblemSolverIT {

    private WireMockServer wireMockServer;
    private LatencyService solver;

    private final String GREEK_GODS_ENDPOINT = "/jabrena/latency-problems/greek";
    private final String ROMAN_GODS_ENDPOINT = "/jabrena/latency-problems/roman";
    private final String NORDIC_GODS_ENDPOINT = "/jabrena/latency-problems/nordic";

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicPort()
                .notifier(new ConsoleNotifier(false)));
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        List<String> testApiUrls = List.of(
            wireMockServer.baseUrl() + GREEK_GODS_ENDPOINT,
            wireMockServer.baseUrl() + ROMAN_GODS_ENDPOINT,
            wireMockServer.baseUrl() + NORDIC_GODS_ENDPOINT
        );

        solver = new LatencyProblemSolver(testApiUrls, Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    private void stubApiFromResource(String path, String resourcePath, int statusCode, int delayMillis) {
        stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/" + resourcePath)
                        .withFixedDelay(delayMillis)));
    }

    private void stubApiUnavailable(String path) {
        stubFor(get(urlEqualTo(path))
                .willReturn(aResponse().withStatus(500)));
    }

    @Test
    @DisplayName("Given all APIs are responsive, when solving, then return correct sum of all gods")
    void happyPath_allGods_correctSum() {
        //Given
        stubFor(
            get(urlEqualTo(GREEK_GODS_ENDPOINT))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/greek_gods.json")
                )
        );
        stubFor(
            get(urlEqualTo(ROMAN_GODS_ENDPOINT))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/roman_gods.json")
                )
        );
        stubFor(
            get(urlEqualTo(NORDIC_GODS_ENDPOINT))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/nordic_gods.json")
                )
        );

        //When
        var result = solver.solve();

        //Then
        final BigInteger expected = new BigInteger("78179288397447443426");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Given APIs return empty lists, when solving, then return zero")
    void emptyListOfGods_sumShouldBeZero() {
        // Given
        stubApiFromResource(GREEK_GODS_ENDPOINT, "empty_gods.json", 200, 0); // Assuming empty_gods.json contains []
        stubApiFromResource(ROMAN_GODS_ENDPOINT, "empty_gods.json", 200, 0);
        stubApiFromResource(NORDIC_GODS_ENDPOINT, "empty_gods.json", 200, 0);

        // When
        BigInteger sum = solver.solve();

        // Then
        assertThat(sum).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Given all APIs fail, when solving, then return zero")
    void allApisFail_sumShouldBeZero() {
        // Given
        stubApiUnavailable(GREEK_GODS_ENDPOINT);
        stubApiUnavailable(ROMAN_GODS_ENDPOINT);
        stubApiUnavailable(NORDIC_GODS_ENDPOINT);

        // When
        BigInteger sum = solver.solve();

        // Then
        assertThat(sum).isEqualTo(BigInteger.ZERO);
    }
}
