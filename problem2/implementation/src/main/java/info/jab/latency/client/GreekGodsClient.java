package info.jab.latency.client;

import java.util.List;

/**
 * Client interface for fetching Greek gods from an API.
 */
public interface GreekGodsClient {
    /**
     * Fetches a list of Greek god names from the specified API URL.
     *
     * @param apiUrl The URL of the Greek Gods API.
     * @return A list of Greek god names. Returns an empty list if an error occurs or no gods are found.
     */
    List<String> fetchGreekGods(String apiUrl);
}
