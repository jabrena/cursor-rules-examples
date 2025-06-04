package info.jab.latency;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for Greek Gods data operations.
 *
 * This service provides the business logic for retrieving Greek god names
 * as defined in the acceptance criteria for US-001.
 */
@Service
public class GreekGodsService {

    /**
     * The complete list of 20 Greek god names as specified in the acceptance criteria.
     * This list is maintained in-memory to ensure fast response times (< 1 second).
     */
    private static final List<String> GREEK_GODS = List.of(
        "Zeus", "Hera", "Poseidon", "Demeter", "Ares", "Athena", "Apollo",
        "Artemis", "Hephaestus", "Aphrodite", "Hermes", "Dionysus", "Hades",
        "Hypnos", "Nike", "Janus", "Nemesis", "Iris", "Hecate", "Tyche"
    );

    /**
     * Retrieves the complete list of Greek god names.
     *
     * @return An immutable list containing exactly 20 Greek god names
     */
    public List<String> getGreekGods() {
        return GREEK_GODS;
    }
}
