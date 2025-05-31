package info.jab.latency.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuration for REST client beans used to call external mythology APIs.
 *
 * Configures timeout and other HTTP client settings as per ADR-001 specifications.
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates a RestClient bean with timeout configuration for external API calls.
     *
     * @param properties the mythology API properties containing timeout configuration
     * @return configured RestClient instance
     */
    @Bean
    public RestClient restClient(MythologyApiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getTimeout());
        factory.setReadTimeout(properties.getTimeout());

        return RestClient.builder()
            .requestFactory(factory)
            .build();
    }
}
