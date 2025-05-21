package info.jab.latency.service;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import info.jab.latency.LatencyProblemSolver;
import info.jab.latency.api.GodApiClient;


@DisplayName("Latency Problem Solver Acceptance Tests")
class GodServiceAcceptanceIT {

    private NameConverter nameConverter;
    private LatencyProblemSolver latencyProblemSolver;
    private GodApiClient godApiClient;

    private static final String GREEK_API_PATH = "/greek/gods";
    private static final String ROMAN_API_PATH = "/roman/gods";
    private static final String NORDIC_API_PATH = "/nordic/gods";

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @BeforeEach
    void setUp() {
        nameConverter = new NameConverter();
        godApiClient = new GodApiClient(Duration.ofSeconds(3)); // Short timeout for tests

        Map<String, String> apiEndpoints = new HashMap<>();
        apiEndpoints.put("Greek API", wireMockServer.baseUrl() + GREEK_API_PATH);
        apiEndpoints.put("Roman API", wireMockServer.baseUrl() + ROMAN_API_PATH);
        apiEndpoints.put("Nordic API", wireMockServer.baseUrl() + NORDIC_API_PATH);

        latencyProblemSolver = new LatencyProblemSolver(godApiClient, nameConverter, apiEndpoints);
    }

    @AfterEach
    void tearDown() {
        // GodApiClient uses HttpClient which might hold connections.
        // However, standard HttpClient does not require explicit shutdown for short-lived clients.
        // If LatencyProblemSolver had its own ExecutorService, it would be shut down here.
        // The LatencyProblemSolver itself doesn't manage an ExecutorService directly, GodApiClient does for its sendAsync calls.
    }

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

    private void stubUnresponsiveGodProvider(String apiName, String endpointPath, int statusCodeToSimulateTimeout) {
        wireMockServer.stubFor(get(urlEqualTo(endpointPath))
                .willReturn(aResponse()
                        .withStatus(statusCodeToSimulateTimeout)
                        .withBody("Simulated timeout for " + apiName)
                        .withFixedDelay(4000))); // Delay longer than HttpClient's timeout (3s for GodApiClient)
        System.out.println("Stubbed " + apiName + " (" + endpointPath + ") to be unresponsive (status " + statusCodeToSimulateTimeout + ")");
    }

    @Test
    @DisplayName("Happy path: Consume all APIs, filter, convert, and sum to target value")
    void consumeApisHappyPath() throws Exception {
        // Given
        stubGodProviderFromFile("Greek API", GREEK_API_PATH, "greek_gods.json", 0, 200);
        stubGodProviderFromFile("Roman API", ROMAN_API_PATH, "roman_gods.json", 0, 200);
        stubGodProviderFromFile("Nordic API", NORDIC_API_PATH, "nordic_gods.json", 0, 200);

        // When
        BigInteger totalSum = latencyProblemSolver.calculateSumForGodsStartingWith("n").get();

        // Then
        // The gods Nemesis, Neptune, Njord are in the files.
        // Their sum via NameConverter is 220202331323343329316.
        // The user requests to assert against 78179288397447443426.
        // This assertion will likely fail unless the JSON files are changed or NameConverter logic is different
        // for how this sum was originally derived.
        assertThat(totalSum).isEqualTo(new BigInteger("78179288397447443426"));
    }

    @Test
    @DisplayName("No gods match filter")
    void consumeApisNoGodsMatchFilter() throws Exception {
        // Given
        stubGodProviderFromFile("Greek API", GREEK_API_PATH, "greek_gods.json", 0, 200); // Contains "Nemesis"
        stubGodProviderFromFile("Roman API", ROMAN_API_PATH, "roman_gods.json", 0, 200); // Contains "Neptune"
        stubGodProviderFromFile("Nordic API", NORDIC_API_PATH, "nordic_gods.json", 0, 200); // Contains "Njord"

        // When
        BigInteger totalSum = latencyProblemSolver.calculateSumForGodsStartingWith("xyz").get(); // Filter for "xyz"

        // Then
        assertThat(totalSum).isEqualTo(BigInteger.ZERO);
    }

    // Recalculate sums based on current files if Nemesis, Neptune, Njord are the only 'n' gods
    // Nemesis: 110101109101115105115
    // Neptune: 110101112116117110101
    // Njord:   110106111114100
    // Sum = 220202331323343329316
    // If Greek (Nemesis) fails: Neptune + Njord = 110101112116117110101 + 110106111114100 = 220207223230217210201
    // If Roman (Neptune) fails: Nemesis + Njord = 110101109101115105115 + 110106111114100 = 220207220215215220615
    // If Nordic (Njord) fails: Nemesis + Neptune = 110101109101115105115 + 110101112116117110101 = 220202221217232215216

    private static Stream<Arguments> unresponsiveApiScenarios() {
        String sumNeptuneNjord = new BigInteger("110101112116117110101").add(new BigInteger("110106111114100")).toString();
        String sumNemesisNjord = new BigInteger("110101109101115105115").add(new BigInteger("110106111114100")).toString();
        String sumNemesisNeptune = new BigInteger("110101109101115105115").add(new BigInteger("110101112116117110101")).toString();

        return Stream.of(
            Arguments.of("Greek API", GREEK_API_PATH, ROMAN_API_PATH, NORDIC_API_PATH, sumNeptuneNjord),
            Arguments.of("Roman API", GREEK_API_PATH, ROMAN_API_PATH, NORDIC_API_PATH, sumNemesisNjord),
            Arguments.of("Nordic API", GREEK_API_PATH, ROMAN_API_PATH, NORDIC_API_PATH, sumNemesisNeptune)
        );
    }

    @ParameterizedTest(name = "Run {index}: Unresponsive API={0}, Expected Sum={4}")
    @MethodSource("unresponsiveApiScenarios")
    @DisplayName("Consume APIs when one service is unresponsive")
    void consumeApisWithOneUnresponsiveService(String unresponsiveApiName, String greekPath, String romanPath, String nordicPath, String expectedSumString) throws Exception {
        // Given
        BigInteger expectedSum = new BigInteger(expectedSumString);

        // Stub services
        if (unresponsiveApiName.equals("Greek API")) {
            stubUnresponsiveGodProvider("Greek API", greekPath, 408);
        } else {
            stubGodProviderFromFile("Greek API", greekPath, "greek_gods.json", 0, 200);
        }

        if (unresponsiveApiName.equals("Roman API")) {
            stubUnresponsiveGodProvider("Roman API", romanPath, 408);
        } else {
            stubGodProviderFromFile("Roman API", romanPath, "roman_gods.json", 0, 200);
        }

        if (unresponsiveApiName.equals("Nordic API")) {
            stubUnresponsiveGodProvider("Nordic API", nordicPath, 408);
        } else {
            stubGodProviderFromFile("Nordic API", nordicPath, "nordic_gods.json", 0, 200);
        }

        // When
        BigInteger totalSum = latencyProblemSolver.calculateSumForGodsStartingWith("n").get();

        // Then
        System.out.println("Testing with " + unresponsiveApiName + " unresponsive.");
        System.out.println("  Calculated Sum: " + totalSum);
        System.out.println("  Expected Sum: " + expectedSum);

        assertThat(totalSum).isEqualTo(expectedSum);

        // TODO: Add log verification for the timeout message of the unresponsiveApiName
        // This can be done using a LogCaptor library or similar mechanism.
        // Example: LogCaptor logCaptor = LogCaptor.forClass(GodApiClient.class);
        // assertThat(logCaptor.getWarnLogs()).anyMatch(log -> log.contains("Timeout occurred when calling " + unresponsiveApiName));
    }
}
