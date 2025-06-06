package info.jab.latency.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import info.jab.latency.client.MythologyApiClient;
import info.jab.latency.dto.GodsResponse;
import info.jab.latency.model.Mythology;

/**
 * Service class for managing mythology data operations.
 *
 * Implements core business logic for mythology data aggregation and response formatting.
 * Coordinates between the API client and controller layers.
 *
 * Based on C4 Component diagram: GodGateway_Component.puml
 */
@Service
public class MythologyService {

    private final MythologyApiClient mythologyApiClient;

    public MythologyService(MythologyApiClient mythologyApiClient) {
        this.mythologyApiClient = Objects.requireNonNull(mythologyApiClient);
    }

    /**
     * Retrieves gods data for the specified mythology type.
     *
     * Validates the mythology parameter, fetches data from external APIs,
     * and formats the response according to the API specification.
     *
     * @param mythology The mythology type as a string
     * @return GodsResponse containing the gods data and metadata
     * @throws IllegalArgumentException if mythology is invalid
     * @throws MythologyServiceException if data retrieval fails
     */
    public GodsResponse getGodsByMythology(Mythology mythology) {
        // Fetch gods data from external API
        try {
            List<String> gods = mythologyApiClient.fetchGodsData(mythology);
            return new GodsResponse(mythology.name(), gods, "external_api");
        //TODO Candidate for Either to avoid throwing exceptions
        } catch (MythologyApiClient.MythologyApiException e) {
            throw new MythologyServiceException(
                    "Failed to retrieve gods data for mythology: " + mythology, e);
        }
    }

    //TODO Remove in next iteration
    public static class MythologyServiceException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public MythologyServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
