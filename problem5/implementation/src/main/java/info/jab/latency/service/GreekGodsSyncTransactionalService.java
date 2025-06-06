package info.jab.latency.service;

import info.jab.latency.entity.GreekGod;
import info.jab.latency.repository.GreekGodsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Transactional service for handling Greek Gods synchronization operations.
 *
 * This service is responsible for the transactional aspects of data synchronization,
 * ensuring that all database operations are properly managed within transactions.
 */
@Service
public class GreekGodsSyncTransactionalService {

    private static final Logger logger = LoggerFactory.getLogger(GreekGodsSyncTransactionalService.class);
    private final GreekGodsRepository greekGodsRepository;

    public GreekGodsSyncTransactionalService(GreekGodsRepository greekGodsRepository) {
        this.greekGodsRepository = greekGodsRepository;
    }

    /**
     * Performs the synchronization within a transaction.
     *
     * @param externalData the data fetched from external API
     * @param syncId the synchronization ID for tracking
     * @return sync result with statistics
     */
    @Transactional
    public SyncResult performTransactionalSync(List<Map<String, Object>> externalData, String syncId) {
        logger.info("[SYNC-{}] Starting transactional synchronization with {} records", syncId, externalData.size());

        try {
            // Transform external data to GreekGod entities
            List<GreekGod> greekGods = transformToGreekGods(externalData, syncId);
            logger.info("[SYNC-{}] Transformed {} records to GreekGod entities", syncId, greekGods.size());

            // Save transformed data to database
            SyncResult syncResult = saveGreekGodsToDatabase(greekGods, syncId);

            logger.info("[SYNC-{}] Transactional sync completed. New: {}, Duplicates: {}, Errors: {}",
                       syncId, syncResult.inserted, syncResult.duplicatesSkipped, syncResult.errors);

            return syncResult;

        } catch (Exception e) {
            logger.error("[SYNC-{}] Transactional sync failed, transaction will be rolled back: {}", syncId, e.getMessage(), e);
            throw e; // Re-throw to trigger transaction rollback
        }
    }

    /**
     * Transforms external API data format to GreekGod entities.
     */
    private List<GreekGod> transformToGreekGods(List<Map<String, Object>> externalData, String syncId) {
        if (externalData.isEmpty()) {
            logger.warn("[SYNC-{}] No external data to transform", syncId);
            return List.of();
        }

        List<GreekGod> transformedGods = externalData.stream()
                .map((Map<String, Object> record) -> mapToGreekGod(record))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (transformedGods.size() < externalData.size()) {
            int skipped = externalData.size() - transformedGods.size();
            logger.warn("[SYNC-{}] Skipped {} invalid records during transformation", syncId, skipped);
        }

        return transformedGods;
    }

    /**
     * Maps a single external API record to a GreekGod entity.
     */
    private GreekGod mapToGreekGod(Map<String, Object> externalRecord) {
        try {
            String name = extractName(externalRecord);

            if (name == null || name.trim().isEmpty()) {
                logger.debug("Skipping record with missing name: {}", externalRecord);
                return null;
            }

            return new GreekGod(name.trim());

        } catch (Exception e) {
            logger.error("Failed to transform record: {} - {}", externalRecord, e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the name field from external API record.
     */
    private String extractName(Map<String, Object> record) {
        String[] nameFields = {"name", "godName", "fullName", "title", "deity"};

        for (String field : nameFields) {
            Object value = record.get(field);
            if (Objects.nonNull(value) && !value.toString().trim().isEmpty()) {
                return value.toString();
            }
        }

        return null;
    }

    /**
     * Saves GreekGod entities to database with duplicate detection.
     */
    private SyncResult saveGreekGodsToDatabase(List<GreekGod> greekGods, String syncId) {
        if (greekGods.isEmpty()) {
            logger.warn("[SYNC-{}] No entities to save", syncId);
            return new SyncResult();
        }

        SyncResult result = new SyncResult();
        List<GreekGod> newGods = new ArrayList<>();

        // Check for duplicates
        for (GreekGod god : greekGods) {
            try {
                if (greekGodsRepository.existsByName(god.name())) {
                    result.duplicatesSkipped++;
                } else {
                    newGods.add(god);
                }
            } catch (Exception e) {
                logger.error("[SYNC-{}] Error checking existence for god '{}': {}", syncId, god.name(), e.getMessage());
                result.errors++;
            }
        }

        // Save new gods
        if (!newGods.isEmpty()) {
            logger.info("[SYNC-{}] Saving {} new gods to database", syncId, newGods.size());

            for (GreekGod god : newGods) {
                try {
                    greekGodsRepository.save(god);
                    result.inserted++;
                } catch (Exception e) {
                    logger.error("[SYNC-{}] Failed to save god '{}': {}", syncId, god.name(), e.getMessage());
                    result.errors++;
                }
            }
        }

        return result;
    }

    /**
     * Result statistics for synchronization operations.
     */
    public static class SyncResult {
        public int inserted = 0;
        public int duplicatesSkipped = 0;
        public int errors = 0;
    }
}
