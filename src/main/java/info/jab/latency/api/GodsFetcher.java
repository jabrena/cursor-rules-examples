package info.jab.latency.api;

import java.io.IOException;
import java.util.List;

/**
 * Interface for fetching God entities from an API.
 */
public interface GodsFetcher {

    /**
     * Fetches a list of God names synchronously from the specified API URL.
     *
     * @param apiUrl The URL of the API to fetch gods from.
     * @return A list of God names.
     *         The list will be empty if an error occurs (e.g., network issue, timeout, parsing error)
     *         or no gods are found.
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     */
    List<String> fetchGods(String apiUrl) throws IOException, InterruptedException;
}
