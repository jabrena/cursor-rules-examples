package info.jab.latency.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException; // Import for specific timeout handling
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client for fetching God entities asynchronously from various APIs.
 * Implements the {@link GodsFetcher} interface.
 */
public class GodApiClient implements GodsFetcher {

    private static final Logger logger = LoggerFactory.getLogger(GodApiClient.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration timeoutDuration;

    /**
     * Constructs a GodApiClient with a specified timeout for HTTP connections and requests.
     *
     * @param timeoutDuration The duration to use for connection and request timeouts.
     */
    public GodApiClient(Duration timeoutDuration) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeoutDuration) // Connection timeout
                .build();
        this.objectMapper = new ObjectMapper();
        this.timeoutDuration = timeoutDuration;
    }

    @Override
    public List<String> fetchGods(String apiUrl) throws IOException, InterruptedException {
        logger.debug("Attempting to fetch gods synchronously from API: {}", apiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(this.timeoutDuration) // Request timeout
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                try {
                    List<String> gods = objectMapper.readValue(response.body(), new TypeReference<List<String>>() {});
                    logger.info("Successfully fetched {} god names from API: {}", gods.size(), apiUrl);
                    return gods;
                } catch (IOException e) {
                    logger.error("Error parsing JSON from API: {}. Response body: {}", apiUrl, response.body(), e);
                    return Collections.emptyList(); // Return empty list on parsing error
                }
            } else {
                logger.warn("Failed to fetch god names from API: {}. Status code: {}. Response body: {}", apiUrl, response.statusCode(), response.body());
                return Collections.emptyList(); // Return empty list on non-200 status
            }
        } catch (HttpTimeoutException e) {
            logger.warn("Timeout occurred when calling API at {}: {}", apiUrl, e.getMessage());
            throw e; // Rethrow to be handled by StructuredTaskScope or caller
        } catch (IOException e) {
            logger.error("IOException when calling API at {}: {}", apiUrl, e.getMessage(), e);
            throw e; // Rethrow to be handled by StructuredTaskScope or caller
        } catch (InterruptedException e) {
            logger.warn("Interrupted when calling API at {}: {}", apiUrl, e.getMessage());
            Thread.currentThread().interrupt(); // Preserve interrupt status
            throw e; // Rethrow to be handled by StructuredTaskScope or caller
        }
    }
}
