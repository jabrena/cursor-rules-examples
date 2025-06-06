package info.jab.latency.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MythologyApiProperties to test URL construction logic and branching scenarios.
 */
class MythologyApiPropertiesTest {

    private MythologyApiProperties properties;

    @BeforeEach
    void setUp() {
        properties = new MythologyApiProperties();
    }

    @Test
    void getUrlForMythology_WhenBaseUrlEndsWithSlash_ShouldAppendMythologyName() {
        // Given
        properties.setBaseUrl("http://localhost:8081/");

        // When
        String result = properties.getUrlForMythology("greek");

        // Then
        assertEquals("http://localhost:8081/greek", result);
    }

    @Test
    void getUrlForMythology_WhenBaseUrlDoesNotEndWithSlash_ShouldAddSlashAndMythologyName() {
        // Given
        properties.setBaseUrl("http://localhost:8081");

        // When
        String result = properties.getUrlForMythology("roman");

        // Then
        assertEquals("http://localhost:8081/roman", result);
    }

    @Test
    @SuppressWarnings("NullAway")
    void getUrlForMythology_WhenBaseUrlIsNull_ShouldThrowIllegalStateException() {
        // Given
        properties.setBaseUrl(null);

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> properties.getUrlForMythology("norse")
        );
        assertEquals("Base URL is not configured", exception.getMessage());
    }

    @Test
    void getUrlForMythology_WhenBaseUrlIsEmpty_ShouldThrowIllegalStateException() {
        // Given
        properties.setBaseUrl("");

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> properties.getUrlForMythology("indian")
        );
        assertEquals("Base URL is not configured", exception.getMessage());
    }

    @Test
    void getUrlForMythology_WhenBaseUrlIsBlank_ShouldThrowIllegalStateException() {
        // Given
        properties.setBaseUrl("   ");

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> properties.getUrlForMythology("celtiberian")
        );
        assertEquals("Base URL is not configured", exception.getMessage());
    }

    @Test
    void timeoutProperty_DefaultValue_ShouldBe5000() {
        // When & Then
        assertEquals(5000, properties.getTimeout());
    }

    @Test
    void timeoutProperty_WhenSet_ShouldReturnSetValue() {
        // Given
        properties.setTimeout(10000);

        // When & Then
        assertEquals(10000, properties.getTimeout());
    }

    @Test
    void baseUrlProperty_WhenSet_ShouldReturnSetValue() {
        // Given
        String expectedUrl = "https://api.mythology.com/v1";
        properties.setBaseUrl(expectedUrl);

        // When & Then
        assertEquals(expectedUrl, properties.getBaseUrl());
    }
}
