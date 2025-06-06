package info.jab.latency.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Background synchronization service for Greek Gods data from external API.
 *
 * This service runs periodically to sync data from external sources.
 * Configured with simple HTTP timeout support and basic error handling.
 *
 * Delegates transactional operations to GreekGodsSyncTransactionalService
 * to ensure proper transaction management.
 */
@Service
public class BackgroundSyncService {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundSyncService.class);
    private final RestClient restClient;
    private final String apiEndpoint;
    private final boolean syncEnabled;
    private final GreekGodsSyncTransactionalService transactionalSyncService;

    public BackgroundSyncService(
            @Qualifier("greekGodsRestClient") RestClient restClient,
            @Value("${external-api.greek-gods.endpoint}") String endpoint,
            @Value("${background-sync.greek-gods.enabled:true}") boolean syncEnabled,
            GreekGodsSyncTransactionalService transactionalSyncService) {

        this.restClient = restClient;
        this.apiEndpoint = endpoint;
        this.syncEnabled = syncEnabled;
        this.transactionalSyncService = transactionalSyncService;

        logger.info("BackgroundSyncService configured: endpoint={}, syncEnabled={}", endpoint, syncEnabled);
    }

    /**
     * Scheduled entry point for synchronization.
     * Coordinates the synchronization process by fetching data and delegating
     * transactional operations to the dedicated transactional service.
     */
    @Scheduled(fixedRateString = "${background-sync.greek-gods.fixed-rate:1800000}",
               initialDelayString = "${background-sync.greek-gods.initial-delay:60000}")
    public void synchronizeData() {
        //TODO review alternatives
        if (!syncEnabled) {
            logger.debug("Background synchronization skipped - disabled via configuration");
            return;
        }

        logger.info("Starting background synchronization");

        try {
            // Fetch data from external API (non-transactional operation)
            List<String> externalData = fetchDataFromExternalAPI();

            if (externalData.isEmpty()) {
                logger.info("No data fetched from external API, skipping synchronization");
                return;
            }

            // Delegate transactional operations to dedicated service
            transactionalSyncService.performTransactionalSync(externalData);

            logger.info("Background synchronization completed successfully");
        } catch (Exception e) {
            logger.error("Failed due to unexpected error: {}", e.getMessage(), e);
            // Note: No need to re-throw as this is the top-level scheduled method
        }
    }

    /**
     * Fetches Greek Gods data from external JSON server.
     */
    private List<String> fetchDataFromExternalAPI() {
        logger.debug("Fetching data from external API: {}", apiEndpoint);

        try {
            String[] result = restClient
                    .get()
                    .uri(apiEndpoint)
                    .retrieve()
                    .body(String[].class);

            if (Objects.isNull(result)) {
                logger.warn("External API returned null response");
                return List.of();
            }

            List<String> resultList = List.of(result);
            logger.debug("Successfully fetched {} records from external API", resultList.size());
            return resultList;
        } catch (RestClientException e) {
            logger.warn("Failed to fetch data from external API: {}", e.getMessage());
            return List.of(); // Return empty list on API failure
        }
    }
}
