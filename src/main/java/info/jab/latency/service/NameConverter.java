package info.jab.latency.service;

import java.math.BigInteger;
import java.util.Locale;

import org.jspecify.annotations.NonNull;

/**
 * Converts string names to their BigInteger decimal representation.
 * Implements the {@link DecimalValueConverter} interface.
 */
public class NameConverter implements DecimalValueConverter {

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
        if (name == null || name.isEmpty()) {
            return BigInteger.ZERO;
        }
        String lowerCaseName = name.toLowerCase(Locale.ROOT);
        StringBuilder decimalValue = new StringBuilder();
        for (int i = 0; i < lowerCaseName.length(); i++) {
            char c = lowerCaseName.charAt(i);
            decimalValue.append((int) c);
        }
        if (decimalValue.length() == 0) {
            return BigInteger.ZERO;
        }
        return new BigInteger(decimalValue.toString());
    }
}
