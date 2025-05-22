package info.jab.latency;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import info.jab.latency.api.GodApiClient;
import info.jab.latency.api.GodsFetcher;
import info.jab.latency.service.DecimalValueConverter;
import info.jab.latency.service.NameConverter;

//Using a preview feature, so we need to suppress the warning
@SuppressWarnings("preview")
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
        // The main logic now uses the structured concurrency result directly
        try {
            List<String> godNames = fetchAllGodsFromApisStructured();
            List<String> filteredGodNames = filterGodsByNameStartsWith(godNames);
            List<BigInteger> decimalValues = convertGodNamesToDecimal(filteredGodNames);
            return sumDecimalValues(decimalValues);
        } catch (InterruptedException | ExecutionException e) {
            // Log the exception or handle it as appropriate for the application
            // For now, rethrow as a runtime exception or return a default value
            Thread.currentThread().interrupt(); // Preserve interrupt status
            throw new RuntimeException("Failed to solve latency problem due to interruption or execution error", e);
        }
    }

    // New method using StructuredTaskScope
    private List<String> fetchAllGodsFromApisStructured() throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<StructuredTaskScope.Subtask<List<String>>> subtasks = apiUrls.stream()
                    .map(apiUrl -> scope.fork(() -> godApiClient.fetchGods(apiUrl)))
                    .toList();

            scope.join().throwIfFailed(); // Wait for all tasks and throw if any failed

            // Collect results from all subtasks
            return subtasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .flatMap(Collection::stream)
                    .toList();
        }
    }

    private Predicate<String> godStartingByn = s -> s.toLowerCase(Locale.ROOT).charAt(0) == 'n';

    private List<String> filterGodsByNameStartsWith(List<String> godNames) {
        List<String> filteredGodNames = godNames.stream()
                .filter(godStartingByn)
                .collect(Collectors.toList());
        return filteredGodNames;
    }

    private List<BigInteger> convertGodNamesToDecimal(List<String> godNames) {
        return godNames.stream()
                .map(nameConverter::convertToDecimal)
                .toList();
    }

    private BigInteger sumDecimalValues(List<BigInteger> decimalValues) {
        return decimalValues.stream()
                .reduce(BigInteger.ZERO, BigInteger::add);
    }
}
