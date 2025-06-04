package info.jab.latency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GreekGodsService.
 *
 * Tests the business logic for retrieving Greek god names.
 */
@DisplayName("GreekGodsService")
class GreekGodsServiceTest {

    private final GreekGodsService greekGodsService = new GreekGodsService();

    /**
     * Expected list of 20 Greek god names as defined in the acceptance criteria
     */
    private static final List<String> EXPECTED_GREEK_GODS = Arrays.asList(
        "Zeus", "Hera", "Poseidon", "Demeter", "Ares", "Athena", "Apollo",
        "Artemis", "Hephaestus", "Aphrodite", "Hermes", "Dionysus", "Hades",
        "Hypnos", "Nike", "Janus", "Nemesis", "Iris", "Hecate", "Tyche"
    );

    @Test
    @DisplayName("Should return exactly 20 Greek god names")
    void shouldReturnExactly20GreekGodNames() {
        // When
        List<String> result = greekGodsService.getGreekGods();

        // Then
        assertThat(result)
            .hasSize(20)
            .containsExactlyInAnyOrderElementsOf(EXPECTED_GREEK_GODS);
    }

    @Test
    @DisplayName("Should return immutable list")
    void shouldReturnImmutableList() {
        // When
        List<String> result = greekGodsService.getGreekGods();

        // Then
        assertThat(result).isUnmodifiable();
    }

    @Test
    @DisplayName("Should return all god names as strings")
    void shouldReturnAllGodNamesAsStrings() {
        // When
        List<String> result = greekGodsService.getGreekGods();

        // Then
        assertThat(result)
            .allSatisfy(godName -> assertThat(godName).isInstanceOf(String.class))
            .allSatisfy(godName -> assertThat(godName).isNotBlank());
    }

    @Test
    @DisplayName("Should return no duplicate entries")
    void shouldReturnNoDuplicateEntries() {
        // When
        List<String> result = greekGodsService.getGreekGods();

        // Then
        assertThat(result)
            .doesNotHaveDuplicates()
            .hasSize(20);
    }

    @Test
    @DisplayName("Should return same list on multiple calls")
    void shouldReturnSameListOnMultipleCalls() {
        // When
        List<String> result1 = greekGodsService.getGreekGods();
        List<String> result2 = greekGodsService.getGreekGods();

        // Then
        assertThat(result1).isEqualTo(result2);
    }
}
