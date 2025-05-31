package info.jab.latency.service;

import java.math.BigInteger;

import org.jspecify.annotations.NonNull;

/**
 * Interface for converting a string value (e.g., a name) to its BigInteger decimal representation.
 */
public interface DecimalValueConverter {

    /**
     * Converts the given name to its decimal representation as a BigInteger.
     *
     * @param name The string name to convert.
     * @return The BigInteger decimal representation of the name. Returns BigInteger.ZERO if the name is null or empty.
     */
    BigInteger convertToDecimal(@NonNull String name);
}
