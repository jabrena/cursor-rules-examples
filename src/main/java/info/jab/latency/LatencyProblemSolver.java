package info.jab.latency;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import info.jab.latency.api.GodApiClient;
import info.jab.latency.api.GodsFetcher;
import info.jab.latency.service.DecimalValueConverter;
import info.jab.latency.service.NameConverter;

public class LatencyProblemSolver implements LatencyService {

    private final GodsFetcher godApiClient;
    private final DecimalValueConverter nameConverter;
    private final List<String> apiUrls;

    public LatencyProblemSolver(List<String> apiUrls, Duration apiTimeout) {
        this.godApiClient = new GodApiClient(apiTimeout);
        this.nameConverter = new NameConverter();
        this.apiUrls = List.copyOf(apiUrls);
    }

    @Override
    public BigInteger solve() {
        // Equivalent to calling calculateSumForGodsStartingWith("") and blocking
        return fetchAllGodsFromApis()
                .thenApply(godNames -> filterGodsByNameStartsWith(godNames)) // Empty prefix means all gods
                .thenApply(this::convertGodNamesToDecimal)
                .thenApply(this::sumDecimalValues)
                .join(); // Block and get the result
    }

    private CompletableFuture<List<String>> fetchAllGodsFromApis() {
        List<CompletableFuture<List<String>>> futures = IntStream.range(0, apiUrls.size())
                .mapToObj(i -> {
                    String apiUrl = apiUrls.get(i);
                    return godApiClient.fetchGodsAsync(apiUrl);
                })
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
    }

    private Predicate<String> godStartingByn = s -> s.toLowerCase(Locale.ROOT).charAt(0) == 'n';

    private List<String> filterGodsByNameStartsWith(List<String> godNames) {
        List<String> filteredGodNames = godNames.stream()
                .filter(godStartingByn)
                .collect(Collectors.toList());
        return filteredGodNames;
    }

    private List<BigInteger> convertGodNamesToDecimal(List<String> godNames) {
        if (Objects.isNull(godNames)) {
            return List.of();
        }
        return godNames.stream()
                .map(nameConverter::convertToDecimal)
                .collect(Collectors.toList());
    }

    private BigInteger sumDecimalValues(List<BigInteger> decimalValues) {
        if (Objects.isNull(decimalValues)) {
            return BigInteger.ZERO;
        }
        return decimalValues.stream()
                .reduce(BigInteger.ZERO, BigInteger::add);
    }
}
