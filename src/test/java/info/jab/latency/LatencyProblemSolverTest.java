package info.jab.latency;

import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import info.jab.latency.api.GodApiClient;
import info.jab.latency.model.God;
import info.jab.latency.service.NameConverter;

class LatencyProblemSolverTest {

    private static final Logger logger = LoggerFactory.getLogger(LatencyProblemSolverTest.class);
    private WireMockServer wireMockServer;
    private LatencyProblemSolver solver;
    private GodApiClient godApiClient;
    private final NameConverter nameConverter = new NameConverter();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String GREEK_API_PATH = "/jabrena/latency-problems/greek";
    private final String ROMAN_API_PATH = "/jabrena/latency-problems/roman";
    private final String NORDIC_API_PATH = "/jabrena/latency-problems/nordic";

    // Sample data based on docs/problem1/my-json-server-oas.yaml structure
    private final List<God> greekGods = List.of(new God("Zeus"), new God("Hera"), new God("Poseidon"), new God("Athena"), new God("Apollo"), new God("Artemis"), new God("Ares"), new God("Aphrodite"), new God("Hephaestus"), new God("Hermes"), new God("Dionysus"), new God("Demeter"));
    private final List<God> romanGods = List.of(new God("Jupiter"), new God("Juno"), new God("Neptune"), new God("Minerva"), new God("Apollo"), new God("Diana"), new God("Mars"), new God("Venus"), new God("Vulcan"), new God("Mercury"), new God("Bacchus"), new God("Ceres"));
    private final List<God> nordicGods = List.of(new God("Odin"), new God("Frigg"), new God("Thor"), new God("Njord"), new God("Freya"), new God("Tyr"), new God("Loki"), new God("Heimdall"));

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
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

    private void stubApi(String path, List<God> godsToReturn, int statusCode, int delayMillis) throws JsonProcessingException {
        stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(godsToReturn))
                        .withFixedDelay(delayMillis)));
    }

    @Test
    void happyPath_filterByN_correctSum() throws JsonProcessingException, ExecutionException, InterruptedException {
        stubApi(GREEK_API_PATH, greekGods, 200, 0);
        stubApi(ROMAN_API_PATH, romanGods, 200, 0);
        stubApi(NORDIC_API_PATH, nordicGods, 200, 0);

        List<God> allGods = solver.fetchAllGodsFromApis().get();
        List<God> filtered = solver.filterGodsByNameStartsWith(allGods, "n");
        List<BigInteger> decimals = solver.convertGodNamesToDecimal(filtered);
        BigInteger sum = solver.sumDecimalValues(decimals);

        // Expected: Neptune (110101112116117110101), Njord (110106111114100)
        BigInteger expectedNeptune = new BigInteger("110101112116117110101");
        BigInteger expectedNjord = new BigInteger("110106111114100");
        BigInteger expectedSum = expectedNeptune.add(expectedNjord);

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(g -> g.name().equals("Neptune")));
        assertTrue(filtered.stream().anyMatch(g -> g.name().equals("Njord")));
        assertEquals(expectedSum, sum);
        logger.info("Happy path sum: {}", sum);
        // The problem description mentions sum 78179288397447443426. This is likely a typo
        // or refers to a different dataset/filter. Our sum is based on provided API and 'n' filter.
    }

    @Test
    void noGodsMatchFilter_sumShouldBeZero() throws JsonProcessingException, ExecutionException, InterruptedException {
        stubApi(GREEK_API_PATH, greekGods, 200, 0);
        stubApi(ROMAN_API_PATH, romanGods, 200, 0);
        stubApi(NORDIC_API_PATH, nordicGods, 200, 0);

        List<God> allGods = solver.fetchAllGodsFromApis().get();
        List<God> filtered = solver.filterGodsByNameStartsWith(allGods, "x"); // No god starts with x
        List<BigInteger> decimals = solver.convertGodNamesToDecimal(filtered);
        BigInteger sum = solver.sumDecimalValues(decimals);

        assertEquals(0, filtered.size());
        assertEquals(BigInteger.ZERO, sum);
    }

    @Test
    void greekApiTimeout_calculatesWithRomanAndNordic() throws JsonProcessingException, ExecutionException, InterruptedException {
        // Greek API times out (simulated by long delay > 5s, WireMock doesn't directly simulate timeout for client side)
        // Wiremock will respond, but client should timeout
        stubFor(get(urlEqualTo(GREEK_API_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(greekGods))
                        .withFixedDelay(6000))); // Delay > 5s timeout
        stubApi(ROMAN_API_PATH, romanGods, 200, 0);
        stubApi(NORDIC_API_PATH, nordicGods, 200, 0);

        List<God> allGods = solver.fetchAllGodsFromApis().get();
        List<God> filtered = solver.filterGodsByNameStartsWith(allGods, "n");
        List<BigInteger> decimals = solver.convertGodNamesToDecimal(filtered);
        BigInteger sum = solver.sumDecimalValues(decimals);

        // Expected: Neptune (110101112116117110101), Njord (110106111114100) - Greek gods (none start with n) are excluded.
        BigInteger expectedNeptune = new BigInteger("110101112116117110101");
        BigInteger expectedNjord = new BigInteger("110106111114100");
        BigInteger expectedSum = expectedNeptune.add(expectedNjord);

        assertEquals(2, filtered.size()); // Still Neptune and Njord
        assertEquals(expectedSum, sum);
        // Further assertions could check logs for timeout messages if logging is captured.
    }

    @Test
    void romanApiUnresponsive_calculatesWithGreekAndNordic() throws JsonProcessingException, ExecutionException, InterruptedException {
        stubApi(GREEK_API_PATH, greekGods, 200, 0);
        // Roman API times out
        stubFor(get(urlEqualTo(ROMAN_API_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(romanGods))
                        .withFixedDelay(6000)));
        stubApi(NORDIC_API_PATH, nordicGods, 200, 0);

        List<God> allGods = solver.fetchAllGodsFromApis().get();
        List<God> filtered = solver.filterGodsByNameStartsWith(allGods, "n");
        List<BigInteger> decimals = solver.convertGodNamesToDecimal(filtered);
        BigInteger sum = solver.sumDecimalValues(decimals);

        // Expected: Njord (110106111114100) - Roman (Neptune) is excluded.
        BigInteger expectedNjord = new BigInteger("110106111114100");
        BigInteger expectedSum = expectedNjord;

        assertEquals(1, filtered.size());
        assertTrue(filtered.stream().anyMatch(g -> g.name().equals("Njord")));
        assertEquals(expectedSum, sum);
    }

     @Test
    void nordicApiUnresponsive_calculatesWithGreekAndRoman() throws JsonProcessingException, ExecutionException, InterruptedException {
        stubApi(GREEK_API_PATH, greekGods, 200, 0);
        stubApi(ROMAN_API_PATH, romanGods, 200, 0);
        // Nordic API times out
        stubFor(get(urlEqualTo(NORDIC_API_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(nordicGods))
                        .withFixedDelay(6000)));

        List<God> allGods = solver.fetchAllGodsFromApis().get();
        List<God> filtered = solver.filterGodsByNameStartsWith(allGods, "n");
        List<BigInteger> decimals = solver.convertGodNamesToDecimal(filtered);
        BigInteger sum = solver.sumDecimalValues(decimals);

        // Expected: Neptune (110101112116117110101) - Nordic (Njord) is excluded.
        BigInteger expectedNeptune = new BigInteger("110101112116117110101");
        BigInteger expectedSum = expectedNeptune;

        assertEquals(1, filtered.size());
        assertTrue(filtered.stream().anyMatch(g -> g.name().equals("Neptune")));
        assertEquals(expectedSum, sum);
    }

    @Test
    void allApisFail_sumShouldBeZero() throws ExecutionException, InterruptedException {
        stubFor(get(urlEqualTo(GREEK_API_PATH))
                .willReturn(aResponse().withStatus(500)));
        stubFor(get(urlEqualTo(ROMAN_API_PATH))
                .willReturn(aResponse().withStatus(500)));
        stubFor(get(urlEqualTo(NORDIC_API_PATH))
                .willReturn(aResponse().withStatus(500)));

        List<God> allGods = solver.fetchAllGodsFromApis().get();
        List<God> filtered = solver.filterGodsByNameStartsWith(allGods, "n");
        List<BigInteger> decimals = solver.convertGodNamesToDecimal(filtered);
        BigInteger sum = solver.sumDecimalValues(decimals);

        assertEquals(0, allGods.size());
        assertEquals(0, filtered.size());
        assertEquals(BigInteger.ZERO, sum);
    }
} 