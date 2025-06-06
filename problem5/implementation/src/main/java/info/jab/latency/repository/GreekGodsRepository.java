package info.jab.latency.repository;

import info.jab.latency.entity.GreekGod;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JDBC repository for Greek God entities.
 * Provides database access operations for the greek_god table.
 */
@Repository
public interface GreekGodsRepository extends CrudRepository<GreekGod, Long> {

    /**
     * Retrieves all Greek god names from the database.
     * This method is optimized to return only the names as strings
     * rather than full entity objects for better performance.
     *
     * @return List of all Greek god names
     */
    @Query("SELECT name FROM greek_god ORDER BY name ASC")
    List<String> findAllGodNames();

    /**
     * Checks if a Greek god with the given name exists.
     *
     * @param name the name to check
     * @return true if exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM greek_god WHERE name = :name")
    boolean existsByName(String name);

}