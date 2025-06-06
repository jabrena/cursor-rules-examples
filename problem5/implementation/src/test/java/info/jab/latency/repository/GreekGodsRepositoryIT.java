package info.jab.latency.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import info.jab.latency.config.PostgreTestContainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@PostgreTestContainers
class GreekGodsRepositoryIT {

    @Autowired
    private GreekGodsRepository greekGodsRepository;

    @Test
    void testFindAllGodNames() {
        // Arrange: Expected 20 Greek god names based on requirements
        int expectedGodCount = 20;

        // Act: Retrieve all god names from repository
        List<String> godNames = greekGodsRepository.findAllGodNames();

        // Assert: Verify we get exactly 20 god names
        assertThat(godNames)
                .isNotNull()
                .hasSize(expectedGodCount)
                .doesNotContainNull()
                .allSatisfy(name -> assertThat(name).isNotBlank());

        // Verify some expected Greek god names are present
        assertThat(godNames)
                .containsAnyOf("Zeus", "Hera", "Poseidon", "Athena", "Apollo", "Artemis");
    }

    @Test
    void testRepositoryIsNotNull() {
        // Basic smoke test to ensure repository is properly injected
        assertThat(greekGodsRepository).isNotNull();
    }
}
