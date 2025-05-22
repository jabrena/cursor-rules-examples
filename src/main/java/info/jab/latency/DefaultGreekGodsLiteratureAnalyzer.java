package info.jab.latency;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Jackson imports for ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default empty implementation of GreekGodsLiteratureAnalyzer.
 * This implementation is intended to be filled out to solve the problem.
 */
public class DefaultGreekGodsLiteratureAnalyzer implements GreekGodsLiteratureAnalyzer {

    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    // Add ObjectMapper instance
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<String> solve(List<String> apiEndpoints) {
        if (apiEndpoints == null || apiEndpoints.size() < 2) {
            System.err.println("Error: API endpoints not configured correctly.");
            return Collections.emptyList();
        }

        String greekGodsApiUrl = apiEndpoints.get(0);
        String wikipediaUrlTemplate = apiEndpoints.get(1);

        List<String> gods = fetchGreekGods(greekGodsApiUrl);
        if (gods.isEmpty()) {
            // System.err.println("Debug: No Greek gods fetched from: " + greekGodsApiUrl);
            return Collections.emptyList();
        }

        Map<String, Integer> literatureLengths = new HashMap<>();
        for (String god : gods) {
            // Basic URL encoding for god name to handle potential special characters in names
            String encodedGodName = URI.create(god).toASCIIString();
            String wikipediaPageUrl = wikipediaUrlTemplate.replace("{greekGod}", encodedGodName);
            int length = fetchWikipediaPageLength(wikipediaPageUrl);
            literatureLengths.put(god, length);
        }

        if (literatureLengths.isEmpty()) {
             // This case might occur if all gods list entries lead to errors in fetching page lengths
            // System.err.println("Debug: Literature lengths map is empty after fetching.");
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
            // System.err.println("Debug: Max literature length is 0. No gods with literature found or all pages were empty/failed.");
            return Collections.emptyList(); // Return empty list if no one has literature > 0
        }

        final int finalMaxLength = maxLength;
        List<String> godsWithMostLiterature = literatureLengths.entrySet().stream()
                .filter(entry -> entry.getValue() == finalMaxLength)
                .map(Map.Entry::getKey)
                .toList();

        // System.out.println("Debug: Gods with most literature (" + finalMaxLength + "): " + godsWithMostLiterature);
        return godsWithMostLiterature;
    }

    private List<String> fetchGreekGods(String apiUrl) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(apiUrl))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            System.err.println("Error: Invalid Greek Gods API URL syntax: " + apiUrl + " - " + e.getMessage());
            return Collections.emptyList();
        }

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                return parseJsonArrayOfStrings(response.body());
            } else {
                System.err.println("Error fetching Greek gods: " + response.statusCode() + " from " + apiUrl);
                return Collections.emptyList();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Exception fetching Greek gods from " + apiUrl + ": " + e.getMessage());
            // e.printStackTrace(); // Keep for deeper debugging if necessary
            return Collections.emptyList();
        }
    }

    // Basic JSON array of strings parser
    private List<String> parseJsonArrayOfStrings(String jsonBody) {
        try {
            // Use ObjectMapper to read the JSON array into a List<String>
            return objectMapper.readValue(jsonBody, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            System.err.println("Error parsing JSON array: " + jsonBody + " - " + e.getMessage());
            // e.printStackTrace(); // Keep for deeper debugging if necessary
            return Collections.emptyList();
        }
    }

    private int fetchWikipediaPageLength(String pageUrl) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(pageUrl))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            System.err.println("Error: Invalid Wikipedia URL syntax: " + pageUrl + " - " + e.getMessage());
            return 0;
        }

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                return response.body().length();
            } else {
                // System.err.println("Debug: Error fetching Wikipedia page " + pageUrl + ": " + response.statusCode());
                return 0;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Exception fetching Wikipedia page " + pageUrl + ": " + e.getMessage());
            // e.printStackTrace(); // Keep for deeper debugging if necessary
            return 0;
        }
    }
}
