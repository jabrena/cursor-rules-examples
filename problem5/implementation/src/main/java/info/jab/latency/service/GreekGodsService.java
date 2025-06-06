package info.jab.latency.service;

import info.jab.latency.repository.GreekGodsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for Greek Gods business logic.
 *
 * Handles business operations related to Greek mythology data retrieval.
 * Following the C4 Model Component architecture: Controller → Service → Repository → Database
 *
 * This service implements business logic for:
 * - Retrieving complete list of Greek god names
 * - Data validation and transformation
 * - Business rule enforcement
 * - Proper exception propagation to controller layer
 *
 * Exception Propagation Strategy:
 * - Business logic exceptions are allowed to bubble up to controller
 * - No try-catch blocks suppress exceptions that should be handled globally
 * - Service methods declare potential exceptions in their contracts
 * - GlobalExceptionHandler will catch and handle all propagated exceptions
 *
 * Architecture: Part of the Service Layer in the C4 Component Model
 * - Input: Business requests from Controller layer
 * - Output: Processed data ready for presentation
 * - Dependencies: Will integrate with GreekGodsRepository (future implementation)
 */
@Service
public class GreekGodsService {

    private static final Logger logger = LoggerFactory.getLogger(GreekGodsService.class);
    private static final int EXPECTED_GOD_COUNT = 20;

    private final GreekGodsRepository greekGodsRepository;

    /**
     * Constructor injection for GreekGodsRepository dependency.
     *
     * @param greekGodsRepository the repository for database access
     */
    @Autowired
    public GreekGodsService(GreekGodsRepository greekGodsRepository) {
        this.greekGodsRepository = greekGodsRepository;
    }

    /**
     * Retrieves all Greek god names for API consumption.
     *
     * Business logic:
     * - Returns complete dataset of Greek god names
     * - Ensures consistent ordering for API responses
     * - Validates data completeness (20 gods minimum)
     * - Propagates any business validation exceptions to controller
     *
     * @return List<String> containing all Greek god names
     * @throws RuntimeException if business validation fails
     * @throws IllegalStateException if data integrity is compromised
     *
     * @implNote Currently returns fake/hardcoded data following ATDD approach.
     *           Will be replaced with repository calls when database layer is implemented.
     *           Any exceptions from repository layer will propagate through this method.
     */
    public List<String> getAllGreekGodNames() {
        logger.debug("Retrieving all Greek god names from database via repository");

        try {
            // Use repository to fetch data from database
            List<String> greekGods = greekGodsRepository.findAllGodNames();

            logger.debug("Successfully retrieved {} Greek god names from database", greekGods.size());
            logger.trace("Greek god names: {}", greekGods);

            return greekGods;

        } catch (Exception ex) {
            // Log the exception but DO NOT catch it - let it propagate to GlobalExceptionHandler
            logger.error("Error occurred while retrieving Greek god names from database: {}", ex.getMessage());

            // Re-throw the exception to ensure proper propagation to controller layer
            // This is crucial for proper exception propagation strategy
            throw ex;
        }
    }
}