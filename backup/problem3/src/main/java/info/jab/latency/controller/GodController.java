package info.jab.latency.controller;

import java.util.Objects;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import info.jab.latency.dto.GodsResponse;
import info.jab.latency.model.Mythology;
import info.jab.latency.service.MythologyService;

/**
 * REST Controller for the God Information Gateway API.
 *
 * Handles HTTP requests for god information retrieval across different mythologies.
 * Implements the unified API endpoint as specified in the OpenAPI specification.
 *
 * Epic: EPIC-001 - God Information Gateway API
 * Feature: FEAT-001 - God Information Gateway API
 * User Story: US-001 - Basic God Information Service
 *
 * Based on OpenAPI specification: gateway-api.yaml
 */
@RestController
@RequestMapping("/api/v1")
public class GodController {

    private final MythologyService mythologyService;

    /**
     * Constructor for GodController.
     *
     * @param mythologyService Service for mythology data operations
     */
    public GodController(MythologyService mythologyService) {
        this.mythologyService = Objects.requireNonNull(mythologyService,
                "mythologyService must not be null");
    }

    /**
     * Get gods by mythology type.
     *
     * Retrieves a list of gods for the specified mythology type.
     * This endpoint supports concurrent access and ensures thread-safe operations.
     *
     * @param mythology The mythology type to retrieve gods for (greek, roman, nordic, indian, celtiberian)
     * @return ResponseEntity containing GodsResponse with god information
     */
    @GetMapping("/gods/{mythology}")
    public ResponseEntity<GodsResponse> getGodsByMythology(@PathVariable String mythology) {

        // Validate mythology parameter
        Optional<Mythology> mythologyEnum = Mythology.fromString(mythology);

        if (mythologyEnum.isEmpty()) {
            return ResponseEntity.badRequest().build();
        } else {
            GodsResponse response = mythologyService.getGodsByMythology(mythologyEnum.get());
            return ResponseEntity.ok(response);
        }
    }
}
