package info.jab.latency.service;

import java.math.BigInteger;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

/**
 * Converts string names to their BigInteger decimal representation.
 * Implements the {@link DecimalValueConverter} interface.
 */
public class NameConverter implements DecimalValueConverter {

    private BigInteger convert(@NonNull String name) {
        if (Objects.isNull(name) || name.isEmpty()) {
            return BigInteger.ZERO;
        }

        String charValuesString = name.chars()
                                      .mapToObj(String::valueOf)
                                      .collect(Collectors.joining());

        return new BigInteger(charValuesString);
    }

    /**
     * Converts the given name to its decimal representation as a BigInteger.
     * Each character in the lowercase version of the name is converted to its ASCII integer value,
     * and these values are concatenated to form a string, which is then converted to a BigInteger.
     *
     * @param name The string name to convert. If null or empty, BigInteger.ZERO is returned.
     * @return The BigInteger decimal representation of the name.
     */
    @Override
    public BigInteger convertToDecimal(@NonNull String name) {
        return convert(name);
    }
}
