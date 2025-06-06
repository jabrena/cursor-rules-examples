package info.jab.latency.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for REST client beans.
 *
 * Centralizes REST client configuration with proper timeout settings
 * and connection management for external API integrations.
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates a configured RestClient bean for Greek Gods external API.
     *
     * @param baseUrl the base URL for the external API
     * @param timeoutMs timeout in milliseconds for both connect and read operations
     * @return configured RestClient instance
     */
    @Bean("greekGodsRestClient")
    public RestClient greekGodsRestClient(
            @Value("${external-api.greek-gods.base-url}") String baseUrl,
            @Value("${external-api.greek-gods.timeout:30000}") int timeoutMs) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
