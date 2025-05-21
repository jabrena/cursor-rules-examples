package info.jab.latency.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class NameConverterTest {

    private final NameConverter nameConverter = new NameConverter();

    @Test
    void convertToDecimal_nullName_shouldReturnZero() {
        assertEquals(BigInteger.ZERO, nameConverter.convertToDecimal(null));
    }

    @Test
    void convertToDecimal_emptyName_shouldReturnZero() {
        assertEquals(BigInteger.ZERO, nameConverter.convertToDecimal(""));
    }

    @ParameterizedTest
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
        BigInteger expected = new BigInteger(expectedDecimalStr);
        assertEquals(expected, nameConverter.convertToDecimal(name));
    }

    @Test
    void convertToDecimal_nameWithSpaces_shouldHandleCorrectly() {
        // Example: "New God" -> "new god" -> n(110)e(101)w(119) (32)g(103)o(111)d(100) -> "11010111932103111100"
        assertEquals(new BigInteger("11010111932103111100"), nameConverter.convertToDecimal("New God"));
    }

    @Test
    void convertToDecimal_nameWithNumbers_shouldHandleCorrectly() {
        // Example: "God1" -> "god1" -> g(103)o(111)d(100)1(49) -> "10311110049"
        assertEquals(new BigInteger("10311110049"), nameConverter.convertToDecimal("God1"));
    }
} 