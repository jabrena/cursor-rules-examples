package info.jab.latency.service;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NameConverterTest {

    private final NameConverter nameConverter = new NameConverter();

    @Test
    @SuppressWarnings("NullAway")
    @DisplayName("Given a null name, when converting to decimal, then return zero")
    void convertToDecimal_nullName_shouldReturnZero() {
        // Given
        String name = null;
        BigInteger expected = BigInteger.ZERO;

        // When
        BigInteger result = nameConverter.convertToDecimal(name);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("Given various names (case-sensitive), when converting to decimal, then return correct ASCII-concatenated decimal value")
    @CsvSource({
            "Zeus, 90101117115",      // Mixed case
            "apollo, 97112111108108111", // All lowercase
            "ATHENA, 658472697865",   // All uppercase
            "a, 97",                  // Single character
            "Z, 90",                   // Single uppercase character
            ",0",                      // Empty string
    })
    void convertToDecimal_variousNames_shouldReturnCorrectDecimal(String name, String expectedDecimalStr) {
        // Given
        BigInteger expected = new BigInteger(expectedDecimalStr);

        // When
        BigInteger result = nameConverter.convertToDecimal(name);

        // Then
        assertThat(result).isEqualTo(expected);
    }
}
