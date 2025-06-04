package info.jab.latency;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for Greek Gods API endpoints.
 *
 * Provides endpoints for retrieving Greek mythology data.
 * This controller implements the requirements defined in US-001.
 */
@RestController
@RequestMapping("/api/v1/gods")
public class GreekGodsController {

    private final GreekGodsService greekGodsService;

    public GreekGodsController(GreekGodsService greekGodsService) {
        this.greekGodsService = greekGodsService;
    }

    /**
     * Retrieves a complete list of Greek god names.
     *
     * @return List of 20 Greek god names as JSON array
     */
    @GetMapping("/greek")
    public List<String> getGreekGods() {
        return greekGodsService.getGreekGods();
    }
}
