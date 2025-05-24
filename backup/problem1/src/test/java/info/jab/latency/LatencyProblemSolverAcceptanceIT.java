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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

@DisplayName("Latency Problem Solver Acceptance Tests")
class LatencyProblemSolverAcceptanceIT {

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
        List<String> apiUrls = List.of(
            wireMockServer.baseUrl() + GREEK_API_PATH,
            wireMockServer.baseUrl() + ROMAN_API_PATH,
            wireMockServer.baseUrl() + NORDIC_API_PATH
        );

        latencyProblemSolver = new LatencyProblemSolver(apiUrls, Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {}

    private String loadJsonFromFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/__files/wiremock/" + filePath)), StandardCharsets.UTF_8);
    }

    private void stubGodProviderFromFile(String apiName, String endpointPath, String jsonFileName, int delayMillis, int statusCode) throws IOException {
        String jsonBody = loadJsonFromFile(jsonFileName);
        wireMockServer.stubFor(get(urlEqualTo(endpointPath))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonBody)
                        .withFixedDelay(delayMillis)));
        System.out.println("Stubbed " + apiName + " (" + endpointPath + ") from file " + jsonFileName + " with status " + statusCode + " and delay " + delayMillis + "ms");
    }

    @Test
    @DisplayName("Happy path: Consume all APIs, convert, and sum all gods")
    void consumeApisHappyPathAllGods() throws Exception {
        // Given
        stubGodProviderFromFile("Greek API", GREEK_API_PATH, "greek_gods.json", 0, 200);
        stubGodProviderFromFile("Roman API", ROMAN_API_PATH, "roman_gods.json", 0, 200);
        stubGodProviderFromFile("Nordic API", NORDIC_API_PATH, "nordic_gods.json", 0, 200);

        // When
        BigInteger totalSum = latencyProblemSolver.solve();

        // Then
        assertThat(totalSum).isEqualTo(EXPECTED_ALL_GODS_SUM);
    }

    @Disabled
    @Test
    @DisplayName("E2E (Real APIs): Use LatencyProblemSolver with real APIs and assert the sum")
    void solve_withRealApis_shouldReturnExpectedSum() {
        // Given
        final String greekApi = "https://my-json-server.typicode.com/jabrena/latency-problems/greek";
        final String romanApi = "https://my-json-server.typicode.com/jabrena/latency-problems/roman";
        final String nordicApi = "https://my-json-server.typicode.com/jabrena/latency-problems/nordic";
        final List<String> apis = List.of(greekApi, romanApi, nordicApi);
        final Duration timeout = Duration.ofSeconds(5);

        LatencyProblemSolver realLatencyProblemSolver = new LatencyProblemSolver(apis, timeout);

        // When
        BigInteger totalSum = realLatencyProblemSolver.solve();

        // Then
        assertThat(totalSum).isEqualTo(EXPECTED_ALL_GODS_SUM);
    }
}
