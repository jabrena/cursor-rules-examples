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

        // When
        BigInteger result = nameConverter.convertToDecimal(name);

        // Then
        assertThat(result).isEqualTo(BigInteger.ZERO);
    }

    @Test
    @DisplayName("Given an empty name, when converting to decimal, then return zero")
    void convertToDecimal_emptyName_shouldReturnZero() {
        // Given
        String name = "";

        // When
        BigInteger result = nameConverter.convertToDecimal(name);

        // Then
        assertThat(result).isEqualTo(BigInteger.ZERO);
    }

    @ParameterizedTest
    @DisplayName("Given various god names, when converting to decimal, then return correct decimal value")
    @CsvSource({
            "Zeus, 122101117115",
            "Hera, 10410111497",
            "Poseidon, 112111115101105100111110",
            "Athena, 9711610410111097",
            "Apollo, 97112111108108111",
            "Artemis, 97114116101109105115",
            "Ares, 97114101115",
            "Aphrodite, 97112104114111100105116101",
            "Hephaestus, 10410111210497101115116117115",
            "Hermes, 104101114109101115",
            "Dionysus, 100105111110121115117115",
            "Demeter, 100101109101116101114",
            // Roman Gods from documentation (first few)
            "Jupiter, 106117112105116101114",
            "Juno, 106117110111",
            "Neptune, 110101112116117110101", // Starts with 'n'
            // Nordic Gods from documentation (first few)
            "Odin, 111100105110",
            "Frigg, 102114105103103",
            "Thor, 116104111114",
            "Njord, 110106111114100" // Starts with 'n'
    })
    void convertToDecimal_variousNames_shouldReturnCorrectDecimal(String name, String expectedDecimalStr) {
        // Given
        BigInteger expected = new BigInteger(expectedDecimalStr);

        // When
        BigInteger result = nameConverter.convertToDecimal(name);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Given a name with spaces, when converting to decimal, then handle correctly")
    void convertToDecimal_nameWithSpaces_shouldHandleCorrectly() {
        // Given
        String nameWithSpaces = "New God";
        BigInteger expected = new BigInteger("11010111932103111100"); // "New God" -> "new god" -> n(110)e(101)w(119) (32)g(103)o(111)d(100)

        // When
        BigInteger result = nameConverter.convertToDecimal(nameWithSpaces);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Given a name with numbers, when converting to decimal, then handle correctly")
    void convertToDecimal_nameWithNumbers_shouldHandleCorrectly() {
        // Given
        String nameWithNumbers = "God1";
        BigInteger expected = new BigInteger("10311110049"); // "God1" -> "god1" -> g(103)o(111)d(100)1(49)

        // When
        BigInteger result = nameConverter.convertToDecimal(nameWithNumbers);

        // Then
        assertThat(result).isEqualTo(expected);
    }
}
