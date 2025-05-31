package info.jab.latency.client;

import info.jab.latency.config.MythologyApiProperties;
import info.jab.latency.model.Mythology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Client service for calling external mythology APIs.
 *
 * Implements error handling as specified in ADR-001.
 * Returns partial results on failure to enable graceful degradation.
 */
@Service
public class MythologyApiClient {

    private static final Logger logger = LoggerFactory.getLogger(MythologyApiClient.class);

    private final RestClient restClient;
    private final MythologyApiProperties properties;

    public MythologyApiClient(RestClient restClient, MythologyApiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /**
     * Fetches gods for a specific mythology from the external API.
     *
     * @param mythology the mythology to fetch gods for
     * @return list of god names, empty list if the API call fails
     */
    public List<String> fetchGods(Mythology mythology) {
        try {
            String url = properties.getUrlForMythology(mythology.getName());
            logger.debug("Fetching {} gods from: {}", mythology.getName(), url);

            List<String> gods = restClient.get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {});

            logger.debug("Successfully fetched {} gods for {}",
                Objects.nonNull(gods) ? gods.size() : 0, mythology.getName());

            return Objects.nonNull(gods) ? gods : Collections.emptyList();

        } catch (IllegalStateException e) {
            logger.error("Configuration error for mythology {}: {}", mythology.getName(), e.getMessage());
            return Collections.emptyList();
        } catch (RestClientException e) {
            logger.warn("Failed to fetch {} gods: {}", mythology.getName(), e.getMessage());
            return Collections.emptyList();
        }
    }
}
