package info.jab.latency.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultWikipediaClient implements WikipediaClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWikipediaClient.class);
    private final HttpClient client;

    public DefaultWikipediaClient(HttpClient client) {
        this.client = client;
    }

    @Override
    public int fetchWikipediaPageLength(String pageUrl) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(pageUrl))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            logger.error("Invalid Wikipedia URL syntax: {} - {}", pageUrl, e.getMessage(), e);
            return 0;
        }

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                int length = response.body().length();
                logger.debug("Successfully fetched Wikipedia page {} with length: {}", pageUrl, length);
                return length;
            } else {
                logger.warn("Error fetching Wikipedia page {}: {}", pageUrl, response.statusCode());
                return 0;
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Exception fetching Wikipedia page {}: {}", pageUrl, e.getMessage(), e);
            return 0;
        }
    }
}
