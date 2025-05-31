package info.jab.latency.controller;

import info.jab.latency.dto.GodDto;
import info.jab.latency.service.MythologyGodsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the mythology gods aggregation API.
 *
 * Provides the main endpoint /api/v1/gods as specified in the acceptance criteria.
 * Implements error handling and logging as per ADR-001 specifications.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class MythologyGodsController {

    private static final Logger logger = LoggerFactory.getLogger(MythologyGodsController.class);

    private final MythologyGodsService mythologyGodsService;

    public MythologyGodsController(MythologyGodsService mythologyGodsService) {
        this.mythologyGodsService = mythologyGodsService;
    }

    /**
     * GET /api/v1/gods endpoint that aggregates mythology data from multiple sources.
     *
     * @return ResponseEntity containing list of GodDto objects with HTTP 200 status
     */
    @GetMapping("/gods")
    public ResponseEntity<List<GodDto>> getAllGods() {
        logger.info("Received request for all mythology gods");

        List<GodDto> gods = mythologyGodsService.getAllGods();

        logger.info("Successfully returned {} gods from {} mythologies",
            gods.size(), gods.stream().map(GodDto::mythology).distinct().count());

        return ResponseEntity.ok(gods);
    }
}
