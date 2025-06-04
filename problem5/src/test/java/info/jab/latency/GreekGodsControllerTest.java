package info.jab.latency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for GreekGodsController.
 *
 * Tests the REST API endpoints for Greek gods data retrieval.
 */
@WebMvcTest(GreekGodsController.class)
@DisplayName("GreekGodsController")
@SuppressWarnings("NullAway")
class GreekGodsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GreekGodsService greekGodsService;

    /**
     * Expected list of 20 Greek god names as defined in the acceptance criteria
     */
    private static final List<String> EXPECTED_GREEK_GODS = Arrays.asList(
        "Zeus", "Hera", "Poseidon", "Demeter", "Ares", "Athena", "Apollo",
        "Artemis", "Hephaestus", "Aphrodite", "Hermes", "Dionysus", "Hades",
        "Hypnos", "Nike", "Janus", "Nemesis", "Iris", "Hecate", "Tyche"
    );

    @Test
    @DisplayName("GET /api/v1/gods/greek should return 200 OK with Greek gods list")
    void getGreekGods_ShouldReturn200WithGreekGodsList() throws Exception {
        // Given
        when(greekGodsService.getGreekGods()).thenReturn(EXPECTED_GREEK_GODS);

        // When & Then
        mockMvc.perform(get("/api/v1/gods/greek")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(20))
                .andExpect(jsonPath("$[0]").value("Zeus"))
                .andExpect(jsonPath("$[1]").value("Hera"))
                .andExpect(jsonPath("$[19]").value("Tyche"));
    }

    @Test
    @DisplayName("GET /api/v1/gods/greek should return JSON array format")
    void getGreekGods_ShouldReturnJsonArrayFormat() throws Exception {
        // Given
        when(greekGodsService.getGreekGods()).thenReturn(EXPECTED_GREEK_GODS);

        // When & Then
        mockMvc.perform(get("/api/v1/gods/greek"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*]").exists());
    }

    @Test
    @DisplayName("GET /api/v1/gods/greek endpoint should exist and be accessible")
    void getGreekGods_EndpointShouldExistAndBeAccessible() throws Exception {
        // Given
        when(greekGodsService.getGreekGods()).thenReturn(EXPECTED_GREEK_GODS);

        // When & Then
        mockMvc.perform(get("/api/v1/gods/greek"))
                .andExpect(status().isOk());
    }
}
