package info.jab.latency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;

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
    @JsonProperty("id") @NonNull Integer id,
    @JsonProperty("mythology") @NonNull String mythology,
    @JsonProperty("god") @NonNull String god
) {
}
