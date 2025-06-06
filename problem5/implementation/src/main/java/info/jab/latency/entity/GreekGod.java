package info.jab.latency.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entity record representing a Greek God in the database.
 * Uses Spring Data JDBC annotations for object-relational mapping.
 *
 * Records are immutable, so Spring Data JDBC will:
 * - Use the @PersistenceCreator constructor when loading from database
 * - Create new instances when saving (the ID will be populated after insert)
 * - Treat entities with null @Id as new entities (INSERT operation)
 */
@Table("greek_god")
public record GreekGod(
    @Id Long id,
    @Column("name") String name
) {

    /**
     * Constructor for creating a new GreekGod without ID (for new entities).
     * When saved, Spring Data JDBC will treat this as a new entity since ID is null.
     *
     * @param name the name of the Greek god
     */
    public GreekGod(String name) {
        this(null, name);
    }

    /**
     * Canonical constructor with all fields.
     * Used by Spring Data JDBC when loading entities from the database.
     *
     * @param id the unique identifier
     * @param name the name of the Greek god
     */
    @PersistenceCreator
    public GreekGod(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}