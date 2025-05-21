package info.jab.latency;

import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach; // Add AssertJ import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import info.jab.latency.api.GodApiClient;
import info.jab.latency.service.NameConverter;

class LatencyProblemSolverIT {

    private static final Logger logger = LoggerFactory.getLogger(LatencyProblemSolverIT.class);
    private WireMockServer wireMockServer;
    private LatencyService solver;
    private GodApiClient godApiClient;
    private final NameConverter nameConverter = new NameConverter();

    private final String GREEK_API_PATH = "/jabrena/latency-problems/greek";
    private final String ROMAN_API_PATH = "/jabrena/latency-problems/roman";
    private final String NORDIC_API_PATH = "/jabrena/latency-problems/nordic";

    private static final BigInteger EXPECTED_NEPTUNE_VALUE = new BigInteger("110101112116117110101");
    private static final BigInteger EXPECTED_NJORD_VALUE = new BigInteger("110106111114100");
    private static final BigInteger NIKE_VALUE = new BigInteger("110105107101");
    private static final BigInteger NEMESIS_VALUE = new BigInteger("110101109101115105115");

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicPort()
                .notifier(new ConsoleNotifier(false)));
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        godApiClient = new GodApiClient(Duration.ofSeconds(5));

        // Use a mutable map for test-specific endpoints
        Map<String, String> testApiEndpoints = new HashMap<>();
        testApiEndpoints.put("Greek API", wireMockServer.baseUrl() + GREEK_API_PATH);
        testApiEndpoints.put("Roman API", wireMockServer.baseUrl() + ROMAN_API_PATH);
        testApiEndpoints.put("Nordic API", wireMockServer.baseUrl() + NORDIC_API_PATH);

        solver = new LatencyProblemSolver(godApiClient, nameConverter, testApiEndpoints);
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

    private void stubApiWithDelayFromResource(String path, String resourcePath, int delayMillis) {
        stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("wiremock/" + resourcePath)
                        .withFixedDelay(delayMillis)));
    }

    private void stubApiUnavailable(String path) {
        stubFor(get(urlEqualTo(path))
                .willReturn(aResponse().withStatus(500)));
    }

    @Test
    @DisplayName("Given all APIs are responsive, when calculating sum for gods starting with 'n', then return correct sum")
    void happyPath_filterByN_correctSum() throws ExecutionException, InterruptedException {
        // Given
        stubApiFromResource(GREEK_API_PATH, "greek_gods.json", 200, 0);
        stubApiFromResource(ROMAN_API_PATH, "roman_gods.json", 200, 0);
        stubApiFromResource(NORDIC_API_PATH, "nordic_gods.json", 200, 0);

        // When
        BigInteger sum = solver.calculateSumForGodsStartingWith("n").get();

        // Then
        // Expected: Neptune, Njord, Nike, Nemesis
        BigInteger expectedSum = EXPECTED_NEPTUNE_VALUE.add(EXPECTED_NJORD_VALUE).add(NIKE_VALUE).add(NEMESIS_VALUE);

        // Assertions will focus on the final sum. Individual steps are now private.
        assertThat(sum).isEqualTo(expectedSum); // Replaced with AssertJ
        logger.info("Happy path sum: {}", sum);
    }

    @Test
    @DisplayName("Given no gods match the filter, when calculating sum, then return zero")
    void noGodsMatchFilter_sumShouldBeZero() throws ExecutionException, InterruptedException {
        // Given
        stubApiFromResource(GREEK_API_PATH, "greek_gods.json", 200, 0);
        stubApiFromResource(ROMAN_API_PATH, "roman_gods.json", 200, 0);
        stubApiFromResource(NORDIC_API_PATH, "nordic_gods.json", 200, 0);

        // When
        BigInteger sum = solver.calculateSumForGodsStartingWith("x").get(); // No god starts with x

        // Then
        // assertEquals(BigInteger.ZERO, sum);
        assertThat(sum).isEqualTo(BigInteger.ZERO); // Replaced with AssertJ
    }

    @Test
    @DisplayName("Given Greek API times out, when calculating sum for 'n', then return sum from Roman and Nordic APIs")
    void greekApiTimeout_calculatesWithRomanAndNordic() throws ExecutionException, InterruptedException {
        // Given
        // Greek API times out (simulated by long delay > 5s, WireMock doesn't directly simulate timeout for client side)
        // Wiremock will respond, but client should timeout
        stubApiWithDelayFromResource(GREEK_API_PATH, "greek_gods.json", 6000); // Delay > 5s timeout
        stubApiFromResource(ROMAN_API_PATH, "roman_gods.json", 200, 0);
        stubApiFromResource(NORDIC_API_PATH, "nordic_gods.json", 200, 0);

        // When
        BigInteger sum = solver.calculateSumForGodsStartingWith("n").get();

        // Then
        // Expected: Neptune (110101112116117110101), Njord (110106111114100) - Greek gods (none start with n) are excluded.
        BigInteger expectedSum = EXPECTED_NEPTUNE_VALUE.add(EXPECTED_NJORD_VALUE);

        // assertEquals(expectedSum, sum);
        assertThat(sum).isEqualTo(expectedSum); // Replaced with AssertJ
    }

    @Test
    @DisplayName("Given Roman API times out, when calculating sum for 'n', then return sum from Greek and Nordic APIs")
    void romanApiUnresponsive_calculatesWithGreekAndNordic() throws ExecutionException, InterruptedException {
        // Given
        stubApiFromResource(GREEK_API_PATH, "greek_gods.json", 200, 0);
        // Roman API times out
        stubApiWithDelayFromResource(ROMAN_API_PATH, "roman_gods.json", 6000);
        stubApiFromResource(NORDIC_API_PATH, "nordic_gods.json", 200, 0);

        // When
        BigInteger sum = solver.calculateSumForGodsStartingWith("n").get();

        // Then
        // Roman (Neptune) is excluded. Expected: Nike, Nemesis (Greek), Njord (Nordic).
        BigInteger expectedSum = NIKE_VALUE.add(NEMESIS_VALUE).add(EXPECTED_NJORD_VALUE);

        assertThat(sum).isEqualTo(expectedSum); // Replaced with AssertJ
    }

     @Test
     @DisplayName("Given Nordic API times out, when calculating sum for 'n', then return sum from Greek and Roman APIs")
    void nordicApiUnresponsive_calculatesWithGreekAndRoman() throws ExecutionException, InterruptedException {
        // Given
        stubApiFromResource(GREEK_API_PATH, "greek_gods.json", 200, 0);
        stubApiFromResource(ROMAN_API_PATH, "roman_gods.json", 200, 0);
        // Nordic API times out
        stubApiWithDelayFromResource(NORDIC_API_PATH, "nordic_gods.json", 6000);

        // When
        BigInteger sum = solver.calculateSumForGodsStartingWith("n").get();

        // Then
        // Nordic (Njord) is excluded. Expected: Nike, Nemesis (Greek), Neptune (Roman).
        BigInteger expectedSum = NIKE_VALUE.add(NEMESIS_VALUE).add(EXPECTED_NEPTUNE_VALUE);

        assertThat(sum).isEqualTo(expectedSum); // Replaced with AssertJ
    }

    @Test
    @DisplayName("Given all APIs fail, when calculating sum, then return zero")
    void allApisFail_sumShouldBeZero() throws ExecutionException, InterruptedException {
        // Given
        stubApiUnavailable(GREEK_API_PATH);
        stubApiUnavailable(ROMAN_API_PATH);
        stubApiUnavailable(NORDIC_API_PATH);

        // When
        BigInteger sum = solver.calculateSumForGodsStartingWith("n").get();

        // Then
        // assertEquals(BigInteger.ZERO, sum);
        assertThat(sum).isEqualTo(BigInteger.ZERO); // Replaced with AssertJ
    }
}
