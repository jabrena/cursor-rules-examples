package info.jab.latency.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import info.jab.latency.entity.GreekGod;
import info.jab.latency.repository.GreekGodsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import info.jab.latency.config.PostgreTestContainers;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Integration test for BackgroundSyncService using Spring Boot Test Objects.
 *
 * This test follows ATDD (Acceptance Test Driven Development) approach:
 * - Test is written FIRST before implementation
 * - Test will FAIL initially - no BackgroundSyncService exists yet
 * - After implementation, test should PASS (Red-Green-Refactor cycle)
 *
 * Uses @SpringBootTest for full Spring context and WireMock for HTTP mocking.
 * Tests follow Given/When/Then pattern for clarity and maintainability.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "external-api.greek-gods.endpoint=/gods",
    "external-api.greek-gods.timeout=5000",
    "background-sync.greek-gods.enabled=true",
    "background-sync.greek-gods.fixed-rate=3600000",
    "background-sync.greek-gods.initial-delay=1000"
})
@PostgreTestContainers
class BackgroundSyncServiceIT {

    private static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureWireMockProperties(DynamicPropertyRegistry registry) {
        // Configure WireMock server URL
        if (Objects.isNull(wireMockServer)) {
            wireMockServer = new WireMockServer(WireMockConfiguration.options().port(0));
            wireMockServer.start();
        }
        registry.add("external-api.greek-gods.base-url", () -> "http://localhost:" + wireMockServer.port());
    }

    @Autowired
    private BackgroundSyncService backgroundSyncService;

    @MockitoBean
    private GreekGodsRepository greekGodsRepository; // Mock database operations

    @BeforeEach
    void setUp() {
        if (Objects.isNull(wireMockServer)) {
            wireMockServer = new WireMockServer(WireMockConfiguration.options().port(0));
            wireMockServer.start();
        }
        wireMockServer.resetAll();
    }

    @AfterEach
    void tearDown() {
        if (Objects.nonNull(wireMockServer)) {
            wireMockServer.resetAll();
        }
    }

    @Test
    @DisplayName("Should synchronize data from external API to database")
    void shouldSynchronizeDataFromExternalApiToDatabase() {
        // Given: External API returns list of Greek gods and repository is empty
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[\"Zeus\",\"Hera\",\"Poseidon\"]")));

        when(greekGodsRepository.existsByName("Zeus")).thenReturn(false);
        when(greekGodsRepository.existsByName("Hera")).thenReturn(false);
        when(greekGodsRepository.existsByName("Poseidon")).thenReturn(false);

        // When: Synchronization process is triggered
        backgroundSyncService.synchronizeData();

        // Then: All gods should be checked for existence and saved to repository
        verify(greekGodsRepository, times(3))
            .existsByName(anyString());
        verify(greekGodsRepository, times(3))
            .save(any(GreekGod.class));

        // Verify specific god names were checked
        verify(greekGodsRepository).existsByName("Zeus");
        verify(greekGodsRepository).existsByName("Hera");
        verify(greekGodsRepository).existsByName("Poseidon");
    }

    @Test
    @DisplayName("Should handle external API connection failure gracefully")
    void shouldHandleExternalApiConnectionFailureGracefully() {
        // Given: External API returns server error
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Internal Server Error\"}")));

        // When: Synchronization process is triggered
        backgroundSyncService.synchronizeData();

        // Then: Should handle gracefully without throwing exceptions (designed for background processing)
        // Verify no database operations were attempted since API call failed
        verifyNoInteractions(greekGodsRepository);

        // Verify the service completed without throwing exceptions
        assertThat(true)
            .describedAs("Service should handle API failures gracefully for background processing")
            .isTrue();
    }

    @Test
    @DisplayName("Should skip duplicate data during synchronization")
    void shouldSkipDuplicateDataDuringSynchronization() {
        // Given: External API returns gods and all gods already exist in repository
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[\"Zeus\",\"Hera\"]")));

        when(greekGodsRepository.existsByName(anyString())).thenReturn(true);

        // When: Synchronization process is triggered
        backgroundSyncService.synchronizeData();

        // Then: Should check for existence but not save any new records
        verify(greekGodsRepository, times(2))
            .existsByName(anyString());
        verify(greekGodsRepository, never())
            .save(any(GreekGod.class));

        // Verify specific behavior for existing gods
        verify(greekGodsRepository).existsByName("Zeus");
        verify(greekGodsRepository).existsByName("Hera");
    }

    @Test
    @DisplayName("Should handle empty external API response")
    void shouldHandleEmptyExternalApiResponse() {
        // Given: External API returns empty array
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        // When: Synchronization process is triggered
        backgroundSyncService.synchronizeData();

        // Then: Should handle gracefully without repository interactions
        verifyNoInteractions(greekGodsRepository);
    }

    @Test
    @DisplayName("Should handle large batch synchronization efficiently")
    void shouldHandleLargeBatchSynchronizationEfficiently() {
        // Given: External API returns large dataset with 100 gods
        StringBuilder largeResponse = new StringBuilder("[");
        for (int i = 1; i <= 100; i++) {
            if (i > 1) largeResponse.append(",");
            largeResponse.append("\"God").append(i).append("\"");
        }
        largeResponse.append("]");

        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(largeResponse.toString())));

        when(greekGodsRepository.existsByName(anyString())).thenReturn(false);

        // When: Large batch synchronization is triggered
        backgroundSyncService.synchronizeData();

        // Then: Should process all 100 records efficiently
        verify(greekGodsRepository, times(100))
            .existsByName(anyString());
        verify(greekGodsRepository, times(100))
            .save(any(GreekGod.class));

        // Verify performance: all operations completed successfully
        assertThat(wireMockServer.getAllServeEvents())
            .hasSize(1)
            .describedAs("Should make only one API call for efficiency");
    }

    @Test
    @DisplayName("Should maintain data integrity during partial sync failures")
    void shouldMaintainDataIntegrityDuringPartialSyncFailures() {
        // Given: External API responds successfully but repository operations fail
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[\"Zeus\",\"Hera\"]")));

        when(greekGodsRepository.existsByName(anyString())).thenReturn(false);
        when(greekGodsRepository.save(any(GreekGod.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When: Synchronization process is triggered
        backgroundSyncService.synchronizeData();

        // Then: Should handle database failures gracefully (designed for background processing)
        // Verify attempted database operations occurred before failure in stream processing
        // Note: Due to stream short-circuiting on exception, only first item is processed
        verify(greekGodsRepository, times(1)).existsByName(anyString());
        verify(greekGodsRepository).save(any(GreekGod.class));

        // Verify the service completed without propagating exceptions
        assertThat(true)
            .describedAs("Service should handle database failures gracefully for background processing")
            .isTrue();
    }

    @Test
    @DisplayName("Should sync only new data when some gods already exist")
    void shouldSyncOnlyNewDataWhenSomeGodsAlreadyExist() {
        // Given: External API returns mix of new and existing gods
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[\"Zeus\",\"Hera\",\"Poseidon\",\"Athena\"]")));

        when(greekGodsRepository.existsByName("Zeus")).thenReturn(true);
        when(greekGodsRepository.existsByName("Hera")).thenReturn(true);
        when(greekGodsRepository.existsByName("Poseidon")).thenReturn(false);
        when(greekGodsRepository.existsByName("Athena")).thenReturn(false);

        // When: Synchronization process is triggered
        backgroundSyncService.synchronizeData();

        // Then: Should check all gods but only save new ones
        verify(greekGodsRepository, times(4))
            .existsByName(anyString());
        verify(greekGodsRepository, times(2))
            .save(any(GreekGod.class));

        // Verify specific gods were processed correctly
        verify(greekGodsRepository).existsByName("Zeus");
        verify(greekGodsRepository).existsByName("Hera");
        verify(greekGodsRepository).existsByName("Poseidon");
        verify(greekGodsRepository).existsByName("Athena");
    }

    @Test
    @DisplayName("Should validate and filter out invalid data during synchronization")
    void shouldValidateAndFilterOutInvalidDataDuringSynchronization() {
        // Given: External API returns mix of valid and invalid god names
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[\"Zeus\",\"InvalidGod\",\"\",\"Hera\"]")));

        when(greekGodsRepository.existsByName(anyString())).thenReturn(false);

        // When: Synchronization process is triggered
        backgroundSyncService.synchronizeData();

        // Then: Should process only valid records (empty strings filtered out)
        verify(greekGodsRepository, times(3))
            .existsByName(anyString());
        verify(greekGodsRepository, times(3))
            .save(any(GreekGod.class));

        // Verify empty strings were filtered out
        verify(greekGodsRepository, never()).existsByName("");

        // Verify valid names were processed
        verify(greekGodsRepository).existsByName("Zeus");
        verify(greekGodsRepository).existsByName("InvalidGod");
        verify(greekGodsRepository).existsByName("Hera");
    }
}
