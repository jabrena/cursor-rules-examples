package info.jab.latency.model;

import java.util.Optional;

/**
 * Enumeration of supported mythology types for the God Information Gateway API.
 *
 * Defines the valid mythology parameters that can be used in API requests.
 * Each mythology type corresponds to a specific external API endpoint.
 *
 * All values are lowercase for consistency and simplicity.
 *
 * Based on OpenAPI specification: gateway-api.yaml and my-json-server-oas.yaml
 */
public enum Mythology {

    greek,
    roman,
    nordic,
    indian,
    celtiberian;

    public String getEndpoint() {
        return name();
    }

    /**
     * Converts a string value to a Mythology enum (case-insensitive).
     *
     * @param value The string value to convert
     * @return The corresponding Mythology enum
     */
    public static Optional<Mythology> fromString(String value) {
        return Optional.of(valueOf(value));
    }
}
