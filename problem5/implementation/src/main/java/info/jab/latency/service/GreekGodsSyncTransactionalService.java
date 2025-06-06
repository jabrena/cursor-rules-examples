package info.jab.latency.service;

import info.jab.latency.entity.GreekGod;
import info.jab.latency.repository.GreekGodsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
     * Combines transformation and persistence logic in a single method.
     *
     * @param externalData the data fetched from external API
     */
    @Transactional
    public void performTransactionalSync(List<String> externalData) {
        logger.info("Starting transactional synchronization with {} records", externalData.size());

        try {
            // Transform external data to GreekGod entities and save with duplicate detection
            long savedCount = externalData.stream()
                    .map(name -> name.trim())
                    .filter(name -> !name.isEmpty())
                    .filter(name -> !greekGodsRepository.existsByName(name))
                    .map(GreekGod::new)
                    .peek(greekGodsRepository::save)
                    .count();

            logger.info("Transactional sync completed successfully - saved {} new records", savedCount);
        } catch (Exception e) {
            logger.error("Transactional sync failed, transaction will be rolled back: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger transaction rollback
        }
    }
}
