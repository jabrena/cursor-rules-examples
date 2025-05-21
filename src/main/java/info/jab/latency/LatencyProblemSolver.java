package info.jab.latency;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.jab.latency.api.GodApiClient;
import info.jab.latency.model.God;
import info.jab.latency.service.NameConverter;

public class LatencyProblemSolver {

    private static final Logger logger = LoggerFactory.getLogger(LatencyProblemSolver.class);

    private final GodApiClient godApiClient;
    private final NameConverter nameConverter;
    private Map<String, String> apiEndpoints;

    public LatencyProblemSolver(Duration apiTimeout) {
        this.godApiClient = new GodApiClient(apiTimeout);
        this.nameConverter = new NameConverter();
        this.apiEndpoints = Map.of(
            "Greek API", "https://my-json-server.typicode.com/jabrena/latency-problems/greek",
            "Roman API", "https://my-json-server.typicode.com/jabrena/latency-problems/roman",
            "Nordic API", "https://my-json-server.typicode.com/jabrena/latency-problems/nordic"
        );
    }

    // Constructor for testing with a mocked client and custom endpoints
    public LatencyProblemSolver(GodApiClient godApiClient, NameConverter nameConverter, Map<String, String> apiEndpoints) {
        this.godApiClient = godApiClient;
        this.nameConverter = nameConverter;
        this.apiEndpoints = apiEndpoints;
    }

    public CompletableFuture<List<God>> fetchAllGodsFromApis() {
        List<CompletableFuture<List<God>>> futures = apiEndpoints.entrySet().stream()
                .map(entry -> godApiClient.fetchGodsAsync(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
    }

    public List<God> filterGodsByNameStartsWith(List<God> gods, String prefix) {
        if (gods == null || prefix == null || prefix.isEmpty()) {
            return List.of();
        }
        List<God> filteredGods = gods.stream()
                .filter(god -> god.name() != null && god.name().toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
        logger.info("Filtered {} gods by prefix '{}'. Result count: {}", gods.size(), prefix, filteredGods.size());
        if (filteredGods.isEmpty()) {
            logger.info("No gods matched the filter prefix '{}'", prefix);
        }
        return filteredGods;
    }

    public List<BigInteger> convertGodNamesToDecimal(List<God> gods) {
        if (gods == null) {
            return List.of();
        }
        return gods.stream()
                .map(god -> nameConverter.convertToDecimal(god.name()))
                .collect(Collectors.toList());
    }

    public BigInteger sumDecimalValues(List<BigInteger> decimalValues) {
        if (decimalValues == null) {
            return BigInteger.ZERO;
        }
        return decimalValues.stream()
                .reduce(BigInteger.ZERO, BigInteger::add);
    }

    public static void main(String[] args) {
        Duration timeout = Duration.ofSeconds(5);
        LatencyProblemSolver solver = new LatencyProblemSolver(timeout);

        solver.fetchAllGodsFromApis().thenAccept(allGods -> {
            logger.info("Total gods fetched from all APIs: {}", allGods.size());

            List<God> filteredGods = solver.filterGodsByNameStartsWith(allGods, "n");
            List<BigInteger> decimalValues = solver.convertGodNamesToDecimal(filteredGods);
            BigInteger totalSum = solver.sumDecimalValues(decimalValues);

            if (filteredGods.isEmpty()) {
                System.out.println("No gods matched the filter.");
                System.out.println("Total sum of decimal values: 0");
            } else {
                System.out.println("Filtered god names converted to decimal: " + decimalValues);
                System.out.println("Total sum of the decimal values: " + totalSum);
            }

        }).join(); // Wait for completion in main thread for demo
    }
} 