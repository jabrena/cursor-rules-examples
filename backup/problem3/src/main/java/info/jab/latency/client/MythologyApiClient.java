package info.jab.latency.client;

import info.jab.latency.model.Mythology;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

/**
 * Client for communicating with external mythology APIs.
 *
 * Handles HTTP requests to external services that provide god information
 * for different mythology types. Implements timeout management and error handling.
 *
 * Based on C4 Component diagram: GodGateway_Component.puml
 */
@Component
public class MythologyApiClient {

    private final RestClient restClient;

    //TODO Move to config class
    /**
     * Constructor for MythologyApiClient.
     *
     * @param baseUrl Base URL for external mythology APIs
     * @param timeout Timeout duration for external API calls
     */
    public MythologyApiClient(
            @Value("${mythology.api.base-url}") String baseUrl,
            @Value("${mythology.api.timeout:10s}") Duration timeout) {

        // Configure HTTP client with timeout settings
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) timeout.toMillis());
        factory.setReadTimeout((int) timeout.toMillis());

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeaders(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .build();
    }

    /**
     * Fetches god data from the external API for the specified mythology.
     *
     * Implements 10-second timeout as specified in the requirements.
     *
     * @param mythology The mythology type to fetch gods for
     * @return List of god names for the specified mythology
     * @throws MythologyApiException if the external API call fails
     */
    public List<String> fetchGodsData(Mythology mythology) {
        try {
            String endpoint = "/" + mythology.getEndpoint();

            return restClient.get()
                    .uri(endpoint)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<String>>() {});

        } catch (Exception e) {
            throw new MythologyApiException(
                    "Failed to fetch gods data for mythology: " + mythology, e);
        }
    }

    /**
     * Custom exception for mythology API related errors.
     */
    public static class MythologyApiException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public MythologyApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
