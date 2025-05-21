package info.jab.latency.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.jab.latency.model.God;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GodApiClient {

    private static final Logger logger = LoggerFactory.getLogger(GodApiClient.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration timeoutDuration;

    public GodApiClient(Duration timeoutDuration) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeoutDuration) // Connection timeout
                .build();
        this.objectMapper = new ObjectMapper();
        this.timeoutDuration = timeoutDuration;
    }

    public CompletableFuture<List<God>> fetchGodsAsync(String apiUrl, String apiName) {
        logger.debug("Attempting to fetch gods from {}: {}", apiName, apiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(this.timeoutDuration) // Request timeout
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    List<God> resultList = Collections.emptyList(); // Initialize with empty list
                    if (response.statusCode() == 200) {
                        try {
                            // Explicitly type the result of readValue
                            List<God> gods = objectMapper.readValue(response.body(), new TypeReference<List<God>>() {});
                            logger.info("Successfully fetched {} gods from {}: {}", gods.size(), apiName, apiUrl);
                            resultList = gods; // Assign if successful
                        } catch (IOException e) {
                            logger.error("Error parsing JSON from {}: {}. Response body: {}", apiName, apiUrl, response.body(), e);
                            // resultList remains Collections.emptyList()
                        }
                    } else {
                        logger.warn("Failed to fetch gods from {}: {}. Status code: {}. Response body: {}", apiName, apiUrl, response.statusCode(), response.body());
                        // resultList remains Collections.emptyList()
                    }
                    return resultList; // Single return point
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof java.net.http.HttpTimeoutException) {
                        logger.warn("Timeout occurred when calling {} API at {}: {}", apiName, apiUrl, ex.getMessage());
                    } else if (ex.getCause() instanceof java.io.IOException) {
                        logger.warn("IOException when calling {} API at {}: {}", apiName, apiUrl, ex.getMessage());
                    } else {
                        logger.error("Unexpected error calling {} API at {}: {}", apiName, apiUrl, ex.getMessage(), ex);
                    }
                    return Collections.<God>emptyList();
                })
                .completeOnTimeout(Collections.<God>emptyList(), this.timeoutDuration.toMillis(), TimeUnit.MILLISECONDS);
    }
} 