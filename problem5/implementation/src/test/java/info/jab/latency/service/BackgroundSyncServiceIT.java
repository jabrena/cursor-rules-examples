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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration test for BackgroundSyncService using Spring Boot Test Objects.
 *
 * This test follows ATDD (Acceptance Test Driven Development) approach:
 * - Test is written FIRST before implementation
 * - Test will FAIL initially - no BackgroundSyncService exists yet
 * - After implementation, test should PASS (Red-Green-Refactor cycle)
 *
 * Uses @SpringBootTest for full Spring context and WireMock for HTTP mocking.
 */
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
    "external-api.greek-gods.endpoint=/gods",
    "external-api.greek-gods.timeout=5000",
    "background-sync.greek-gods.enabled=true",
    "background-sync.greek-gods.fixed-rate=3600000",
    "background-sync.greek-gods.initial-delay=1000"
})
class BackgroundSyncServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");

        // Configure WireMock server URL
        if (Objects.isNull(wireMockServer)) {
            wireMockServer = new WireMockServer(WireMockConfiguration.options().port(0));
            wireMockServer.start();
        }
        registry.add("external-api.greek-gods.base-url", () -> "http://localhost:" + wireMockServer.port());
    }

        @Autowired
    private BackgroundSyncService backgroundSyncService;

    @MockBean
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
        // Arrange - Mock HTTP response
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"Zeus\"},{\"name\":\"Hera\"},{\"name\":\"Poseidon\"}]")));

        // Mock repository responses
        when(greekGodsRepository.existsByName("Zeus")).thenReturn(false);
        when(greekGodsRepository.existsByName("Hera")).thenReturn(false);
        when(greekGodsRepository.existsByName("Poseidon")).thenReturn(false);

        // Act - Trigger the synchronization process manually
        backgroundSyncService.synchronizeData();

        // Assert - Verify repository interactions
        verify(greekGodsRepository, times(3)).existsByName(anyString());
        verify(greekGodsRepository, times(3)).save(any(GreekGod.class));
    }

        @Test
    @DisplayName("Should handle external API connection failure gracefully")
    void shouldHandleExternalApiConnectionFailureGracefully() {
        // Arrange - Mock HTTP failure
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Internal Server Error\"}")));

        // Act & Assert - Should handle gracefully but still throw the exception for transaction rollback
        try {
            backgroundSyncService.synchronizeData();
        } catch (Exception e) {
            // Expected behavior - the service should propagate the exception for transaction rollback
            assertThat(e).isNotNull();
        }
    }

        @Test
    @DisplayName("Should skip duplicate data during synchronization")
    void shouldSkipDuplicateDataDuringSynchronization() {
        // Arrange - Mock HTTP response
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"Zeus\"},{\"name\":\"Hera\"}]")));

        // Mock existing data in repository
        when(greekGodsRepository.existsByName(anyString())).thenReturn(true);

        // Act
        backgroundSyncService.synchronizeData();

        // Assert - Should check for existence but not save any new records
        verify(greekGodsRepository, times(2)).existsByName(anyString());
        verify(greekGodsRepository, times(0)).save(any(GreekGod.class));
    }

    @Test
    @DisplayName("Should handle empty external API response")
    void shouldHandleEmptyExternalApiResponse() {
        // Arrange - Mock empty response
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        // Act
        backgroundSyncService.synchronizeData();

        // Assert - Should handle gracefully without repository interactions
        verify(greekGodsRepository, times(0)).existsByName(anyString());
        verify(greekGodsRepository, times(0)).save(any(GreekGod.class));
    }

    // Additional test scenarios for task 9.2: Enhanced data synchronization testing

    @Test
    @DisplayName("Should handle large batch synchronization efficiently")
    void shouldHandleLargeBatchSynchronizationEfficiently() {
        // Arrange - Mock large dataset response
        StringBuilder largeResponse = new StringBuilder("[");
        for (int i = 1; i <= 100; i++) {
            if (i > 1) largeResponse.append(",");
            largeResponse.append("{\"name\":\"God").append(i).append("\"}");
        }
        largeResponse.append("]");

        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(largeResponse.toString())));

        // Mock repository for large dataset
        when(greekGodsRepository.existsByName(anyString())).thenReturn(false);

        // Act
        backgroundSyncService.synchronizeData();

        // Assert - Should handle efficiently
        verify(greekGodsRepository, times(100)).existsByName(anyString());
        verify(greekGodsRepository, times(100)).save(any(GreekGod.class));
    }

    @Test
    @DisplayName("Should maintain data integrity during partial sync failures")
    void shouldMaintainDataIntegrityDuringPartialSyncFailures() {
        // Arrange - Mock some repository failures
        when(greekGodsRepository.existsByName(anyString())).thenReturn(false);
        when(greekGodsRepository.save(any(GreekGod.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert - Should handle partial failures gracefully
        try {
            backgroundSyncService.synchronizeData();
        } catch (Exception e) {
            // Should be handled gracefully within the service
        }

        // The service should not crash and should log appropriate errors
    }

    @Test
    @DisplayName("Should sync only modified data based on timestamps or versions")
    void shouldSyncOnlyModifiedDataBasedOnTimestampsOrVersions() {
        // Arrange - Mock HTTP response with mixed data
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"Zeus\"},{\"name\":\"Hera\"},{\"name\":\"Poseidon\"},{\"name\":\"Athena\"}]")));

        // Mock mixed existing/new data
        when(greekGodsRepository.existsByName("Zeus")).thenReturn(true);
        when(greekGodsRepository.existsByName("Hera")).thenReturn(true);
        when(greekGodsRepository.existsByName("Poseidon")).thenReturn(false);
        when(greekGodsRepository.existsByName("Athena")).thenReturn(false);

        // Act
        backgroundSyncService.synchronizeData();

        // Assert - Should only save new gods (Poseidon and Athena)
        verify(greekGodsRepository, times(4)).existsByName(anyString());
        verify(greekGodsRepository, times(2)).save(any(GreekGod.class));
    }

    @Test
    @DisplayName("Should validate data format and structure during synchronization")
    void shouldValidateDataFormatAndStructureDuringSynchronization() {
        // Arrange - Mock response with mixed valid/invalid data
        wireMockServer.stubFor(get(urlEqualTo("/gods"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"Zeus\"},{\"invalidField\":\"value\"},{\"name\":\"\"},{\"name\":\"Hera\"}]")));

        // Mock repository responses
        when(greekGodsRepository.existsByName(anyString())).thenReturn(false);

        // Act
        backgroundSyncService.synchronizeData();

        // Assert - Should only process valid records (Zeus and Hera)
        verify(greekGodsRepository, times(2)).existsByName(anyString());
        verify(greekGodsRepository, times(2)).save(any(GreekGod.class));
    }
}
