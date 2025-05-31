package info.jab.latency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data transfer object representing a mythology god in the API response.
 *
 * This DTO follows the expected response structure defined in the acceptance criteria:
 * {
 *   "id": 1,
 *   "mythology": "greek",
 *   "god": "Zeus"
 * }
 */
public record GodDto(
    @JsonProperty("id") Integer id,
    @JsonProperty("mythology") String mythology,
    @JsonProperty("god") String god
) { }
