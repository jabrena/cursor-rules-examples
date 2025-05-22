package info.jab.latency;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import info.jab.latency.client.DefaultGreekGodsClient;
import info.jab.latency.client.DefaultWikipediaClient;
import info.jab.latency.client.GreekGodsClient;
import info.jab.latency.client.WikipediaClient;

/**
 * Default implementation of GreekGodsLiteratureAnalyzer.
 * This implementation is intended to be filled out to solve the problem.
 */
public class DefaultGreekGodsLiteratureAnalyzer implements GreekGodsLiteratureAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultGreekGodsLiteratureAnalyzer.class);

    private final GreekGodsClient greekGodsClient;
    private final WikipediaClient wikipediaClient;

    // Constructor for dependency injection
    public DefaultGreekGodsLiteratureAnalyzer(GreekGodsClient greekGodsClient, WikipediaClient wikipediaClient) {
        this.greekGodsClient = greekGodsClient;
        this.wikipediaClient = wikipediaClient;
    }

    // Default constructor that creates default clients
    public DefaultGreekGodsLiteratureAnalyzer() {
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        this.greekGodsClient = new DefaultGreekGodsClient(httpClient, objectMapper);
        this.wikipediaClient = new DefaultWikipediaClient(httpClient);
    }

    @Override
    public List<String> solve(List<String> apiEndpoints) {
        if (apiEndpoints == null || apiEndpoints.size() < 2) {
            logger.error("API endpoints not configured correctly. Expected at least 2, got: {}", apiEndpoints != null ? apiEndpoints.size() : "null");
            return Collections.emptyList();
        }

        String greekGodsApiUrl = apiEndpoints.get(0);
        String wikipediaUrlTemplate = apiEndpoints.get(1);

        logger.debug("Fetching Greek gods from: {}", greekGodsApiUrl);
        List<String> gods = greekGodsClient.fetchGreekGods(greekGodsApiUrl);
        if (gods.isEmpty()) {
            logger.warn("No Greek gods fetched from: {}. Returning empty list.", greekGodsApiUrl);
            return Collections.emptyList();
        }

        Map<String, Integer> literatureLengths = new HashMap<>();
        for (String god : gods) {
            // Basic URL encoding for god name to handle potential special characters in names
            String encodedGodName = URI.create(god).toASCIIString(); // Consider a more robust encoding if names are complex
            String wikipediaPageUrl = wikipediaUrlTemplate.replace("{greekGod}", encodedGodName);
            logger.debug("Fetching Wikipedia page length for god: {}, URL: {}", god, wikipediaPageUrl);
            int length = wikipediaClient.fetchWikipediaPageLength(wikipediaPageUrl);
            literatureLengths.put(god, length);
        }

        if (literatureLengths.isEmpty() && !gods.isEmpty()) {
             // This case might occur if all gods list entries lead to errors in fetching page lengths
            logger.warn("Literature lengths map is empty after fetching, although gods were found. This could mean all Wikipedia page fetches failed.");
            return Collections.emptyList();
        } else if (literatureLengths.isEmpty()) {
            logger.warn("No literature lengths found and no gods were initially fetched or processed.");
            return Collections.emptyList();
        }

        int maxLength = 0;
        for (int length : literatureLengths.values()) {
            if (length > maxLength) {
                maxLength = length;
            }
        }

        // If all pages had 0 length (e.g. all 404s or errors), maxLength will be 0.
        // In this specific problem, if max length is 0, it means no god had discoverable literature.
        if (maxLength == 0) {
            logger.info("Max literature length is 0. No gods with literature found or all pages were empty/failed.");
            return Collections.emptyList(); // Return empty list if no one has literature > 0
        }

        final int finalMaxLength = maxLength;
        List<String> result = literatureLengths.entrySet().stream()
                .filter(entry -> entry.getValue() == finalMaxLength)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        logger.info("Gods with most literature ({} characters): {}", finalMaxLength, result);
        return result;
    }
}
