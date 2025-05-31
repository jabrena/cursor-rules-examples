package info.jab.latency.config;

import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for external mythology APIs.
 *
 * Maps to the 'mythology' properties in application.yml.
 * Uses a single base URL and constructs specific endpoints by appending mythology names.
 */
@Component
@ConfigurationProperties(prefix = "mythology")
public class MythologyApiProperties {

    @SuppressWarnings("NullAway.Init")
    private String baseUrl;
    private int timeout = 5000; // Default 5 seconds

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Constructs the full URL for a specific mythology endpoint.
     *
     * @param mythologyName the name of the mythology (e.g., "greek", "roman")
     * @return the complete URL for the mythology API endpoint
     */
    public String getUrlForMythology(String mythologyName) {
        if (Objects.isNull(baseUrl) || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("Base URL is not configured");
        }
        return baseUrl.endsWith("/") ? baseUrl + mythologyName : baseUrl + "/" + mythologyName;
    }
}
