package info.jab.latency.dto;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Error response DTO for the God Information Gateway API.
 *
 * Provides a standardized error response format for all API errors.
 * This ensures consistent error handling and meaningful error messages for clients.
 *
 * Based on OpenAPI specification: gateway-api.yaml
 */
public final class ErrorResponse {

    private final String error;
    private final String message;
    private final Instant timestamp;
    private final String path;
    private final Map<String, Object> details;

    /**
     * Constructor for creating an ErrorResponse.
     *
     * @param error Error code identifying the type of error
     * @param message Human-readable error message
     * @param path The request path that caused the error
     */
    public ErrorResponse(String error, String message, String path) {
        this(error, message, path, Map.of());
    }

    /**
     * Constructor for creating an ErrorResponse with additional details.
     *
     * @param error Error code identifying the type of error
     * @param message Human-readable error message
     * @param path The request path that caused the error
     * @param details Additional error details (optional)
     */
    public ErrorResponse(String error, String message, String path, Map<String, Object> details) {
        this.error = Objects.requireNonNull(error, "error must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.path = Objects.requireNonNull(path, "path must not be null");
        this.timestamp = Instant.now();
        this.details = Objects.requireNonNull(details, "details must not be null");
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ErrorResponse that = (ErrorResponse) obj;
        return Objects.equals(error, that.error) &&
                Objects.equals(message, that.message) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(path, that.path) &&
                Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, message, timestamp, path, details);
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", path='" + path + '\'' +
                ", details=" + details +
                '}';
    }
}
