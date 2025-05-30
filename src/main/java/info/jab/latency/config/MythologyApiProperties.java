package info.jab.latency.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Configuration properties for external mythology APIs.
 *
 * Maps to the 'mythology' properties in application.yml.
 */
@Component
@ConfigurationProperties(prefix = "mythology")
public class MythologyApiProperties {

    private Map<String, String> apis;
    private int timeout = 5000; // Default 5 seconds

    public Map<String, String> getApis() {
        return apis;
    }

    public void setApis(Map<String, String> apis) {
        this.apis = apis;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
