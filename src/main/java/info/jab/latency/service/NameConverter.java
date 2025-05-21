package info.jab.latency.service;

import java.math.BigInteger;

public class NameConverter {

    public BigInteger convertToDecimal(String name) {
        if (name == null || name.isEmpty()) {
            return BigInteger.ZERO;
        }
        String lowerCaseName = name.toLowerCase();
        StringBuilder decimalValue = new StringBuilder();
        for (char c : lowerCaseName.toCharArray()) {
            decimalValue.append((int) c);
        }
        if (decimalValue.length() == 0) {
            return BigInteger.ZERO;
        }
        return new BigInteger(decimalValue.toString());
    }
} 