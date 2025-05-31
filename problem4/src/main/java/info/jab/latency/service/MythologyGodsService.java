package info.jab.latency.service;

import info.jab.latency.client.MythologyApiClient;
import info.jab.latency.dto.GodDto;
import info.jab.latency.model.Mythology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service for aggregating mythology gods data from multiple external APIs.
 *
 * Implements parallel processing and data transformation as specified in ADR-001.
 * Uses CompletableFuture for concurrent calls to external services.
 */
@Service
public class MythologyGodsService {

    private static final Logger logger = LoggerFactory.getLogger(MythologyGodsService.class);

    private final MythologyApiClient mythologyApiClient;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public MythologyGodsService(MythologyApiClient mythologyApiClient) {
        this.mythologyApiClient = mythologyApiClient;
    }

    /**
     * Aggregates gods data from all mythology APIs in parallel.
     *
     * @return list of GodDto objects containing aggregated data from all mythologies
     */
    public List<GodDto> getAllGods() {
        logger.debug("Starting aggregation of all mythology gods data");

        // Reset counter for each request to ensure consistent IDs
        idCounter.set(1);

        // Create parallel futures for each mythology API call
        List<CompletableFuture<List<GodDto>>> futures = Arrays.stream(Mythology.values())
            .map(this::fetchGodsAsync)
            .toList();

        // Wait for all futures to complete and collect results
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture<?>[0])
        );

        List<GodDto> allGods = allFutures.thenApply(v ->
            futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList())
        ).join();

        logger.debug("Completed aggregation with {} total gods", allGods.size());
        return allGods;
    }

    /**
     * Asynchronously fetches gods for a specific mythology and transforms them to DTOs.
     *
     * @param mythology the mythology to fetch
     * @return CompletableFuture containing list of GodDto objects
     */
    private CompletableFuture<List<GodDto>> fetchGodsAsync(Mythology mythology) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> godNames = mythologyApiClient.fetchGods(mythology);

            return godNames.stream()
                .map(godName -> new GodDto(
                    idCounter.getAndIncrement(),
                    mythology.getName(),
                    godName
                ))
                .collect(Collectors.toList());
        });
    }
}
