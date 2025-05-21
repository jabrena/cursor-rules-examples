package info.jab.latency.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for fetching God entities asynchronously from an API.
 */
public interface GodsFetcher {

    /**
     * Fetches a list of Gods asynchronously from the specified API URL.
     *
     * @param apiUrl The URL of the API to fetch gods from.
     * @param apiName A descriptive name for the API (used for logging/debugging).
     * @return A CompletableFuture that, when completed, will contain a list of God objects.
     *         The list will be empty if an error occurs or no gods are found.
     */
    CompletableFuture<List<String>> fetchGodsAsync(String apiUrl, String apiName);
}
