package info.jab.latency.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class DefaultGreekGodsClient implements GreekGodsClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultGreekGodsClient.class);
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public DefaultGreekGodsClient(HttpClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<String> fetchGreekGods(String apiUrl) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(apiUrl))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            logger.error("Invalid Greek Gods API URL syntax: {} - {}", apiUrl, e.getMessage(), e);
            return Collections.emptyList();
        }

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                List<String> parsedGods = parseJsonArrayOfStrings(response.body());
                logger.debug("Successfully fetched and parsed {} gods from {}", parsedGods.size(), apiUrl);
                return parsedGods;
            } else {
                logger.error("Error fetching Greek gods: {} from {}", response.statusCode(), apiUrl);
                return Collections.emptyList();
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Exception fetching Greek gods from {}: {}", apiUrl, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<String> parseJsonArrayOfStrings(String jsonBody) {
        try {
            List<String> gods = objectMapper.readValue(jsonBody, new TypeReference<List<String>>() {});
            logger.trace("Parsed JSON array of strings: {}", gods);
            return gods;
        } catch (IOException e) {
            logger.error("Error parsing JSON array: {} - {}", jsonBody, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
