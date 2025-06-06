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

        long startTime = System.currentTimeMillis();
        String syncId = generateSyncId();

        logger.info("[SYNC-{}] Starting background synchronization", syncId);

        try {
            // Fetch data from external API (non-transactional operation)
            List<Map<String, Object>> externalData = fetchDataFromExternalAPI();
            logger.info("[SYNC-{}] Fetched {} records from external API", syncId, externalData.size());

            // Delegate transactional operations to dedicated service
            GreekGodsSyncTransactionalService.SyncResult syncResult =
                transactionalSyncService.performTransactionalSync(externalData, syncId);

            // Log successful completion
            long duration = System.currentTimeMillis() - startTime;
            logger.info("[SYNC-{}] Completed successfully in {}ms. New: {}, Duplicates: {}, Errors: {}",
                       syncId, duration, syncResult.inserted, syncResult.duplicatesSkipped, syncResult.errors);

        } catch (RestClientException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[SYNC-{}] Failed due to API error after {}ms: {}", syncId, duration, e.getMessage());
            // Note: No need to re-throw as this is the top-level scheduled method
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[SYNC-{}] Failed due to unexpected error after {}ms: {}", syncId, duration, e.getMessage(), e);
            // Note: No need to re-throw as this is the top-level scheduled method
        }
    }

    /**
     * Fetches Greek Gods data from external JSON server.
     */
    private List<Map<String, Object>> fetchDataFromExternalAPI() {
        logger.debug("Fetching data from external API: {}", apiEndpoint);

        List<Map<String, Object>> result = restClient
                .get()
                .uri(apiEndpoint)
                .retrieve()
                .body(List.class);

        if (Objects.isNull(result)) {
            logger.warn("External API returned null response");
            return List.of();
        }

        logger.debug("Successfully fetched {} records from external API", result.size());
        return result;
    }

    /**
     * Generates a unique sync ID for tracking.
     */
    private String generateSyncId() {
        return String.format("%d-%04d",
                           System.currentTimeMillis() / 1000,
                           (int)(Math.random() * 10000));
    }
}
