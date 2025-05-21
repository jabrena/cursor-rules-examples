package info.jab.latency;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.jab.latency.api.GodApiClient;
import info.jab.latency.api.GodsFetcher;
import info.jab.latency.service.DecimalValueConverter;
import info.jab.latency.service.NameConverter;

public class LatencyProblemSolver implements LatencyService {

    private static final Logger logger = LoggerFactory.getLogger(LatencyProblemSolver.class);

    private final GodsFetcher godApiClient;
    private final DecimalValueConverter nameConverter;
    private Map<String, String> apiEndpoints;

    // Primary constructor with dependency injection
    public LatencyProblemSolver(GodsFetcher godApiClient, DecimalValueConverter nameConverter, Map<String, String> apiEndpoints) {
        this.godApiClient = godApiClient;
        this.nameConverter = nameConverter;
        this.apiEndpoints = Map.copyOf(apiEndpoints);
    }

    // Convenience constructor
    public LatencyProblemSolver(Duration apiTimeout) {
        this(new GodApiClient(apiTimeout), new NameConverter(), Map.of(
            "Greek API", "https://my-json-server.typicode.com/jabrena/latency-problems/greek",
            "Roman API", "https://my-json-server.typicode.com/jabrena/latency-problems/roman",
            "Nordic API", "https://my-json-server.typicode.com/jabrena/latency-problems/nordic"
        ));
    }

    @Override
    public CompletableFuture<BigInteger> calculateSumForGodsStartingWith(String prefix) {
        return fetchAllGodsFromApis()
                .thenApply(godNames -> filterGodsByNameStartsWith(godNames, prefix))
                .thenApply(this::convertGodNamesToDecimal)
                .thenApply(this::sumDecimalValues);
    }

    private CompletableFuture<List<String>> fetchAllGodsFromApis() {
        List<CompletableFuture<List<String>>> futures = apiEndpoints.entrySet().stream()
                .map(entry -> godApiClient.fetchGodsAsync(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
    }

    private List<String> filterGodsByNameStartsWith(List<String> godNames, String prefix) {
        if (godNames == null || prefix == null || prefix.isEmpty()) {
            return List.of();
        }
        List<String> filteredGodNames = godNames.stream()
                .filter(name -> name != null && name.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
        logger.info("Filtered {} god names by prefix '{}'. Result count: {}", godNames.size(), prefix, filteredGodNames.size());
        if (filteredGodNames.isEmpty()) {
            logger.info("No god names matched the filter prefix '{}'", prefix);
        }
        return filteredGodNames;
    }

    private List<BigInteger> convertGodNamesToDecimal(List<String> godNames) {
        if (godNames == null) {
            return List.of();
        }
        return godNames.stream()
                .map(nameConverter::convertToDecimal)
                .collect(Collectors.toList());
    }

    private BigInteger sumDecimalValues(List<BigInteger> decimalValues) {
        if (decimalValues == null) {
            return BigInteger.ZERO;
        }
        return decimalValues.stream()
                .reduce(BigInteger.ZERO, BigInteger::add);
    }
}
