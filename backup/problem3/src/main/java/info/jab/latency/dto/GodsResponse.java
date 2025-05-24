package info.jab.latency.dto;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for the God Information Gateway API.
 *
 * Represents the standardized response format for god information requests.
 * This DTO ensures consistent response structure across all mythology types.
 *
 * Based on OpenAPI specification: gateway-api.yaml
 */
public final class GodsResponse {

    @JsonProperty("mythology")
    private final String mythology;

    @JsonProperty("gods")
    private final List<String> gods;

    @JsonProperty("count")
    private final int count;

    @JsonProperty("source")
    private final String source;

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private final Instant timestamp;

    /**
     * Constructor for creating a GodsResponse.
     *
     * @param mythology The mythology type (greek, roman, nordic, indian, celtiberian)
     * @param gods List of god names for the specified mythology
     * @param source Source of the data (typically "external_api")
     */
    public GodsResponse(String mythology, List<String> gods, String source) {
        this.mythology = Objects.requireNonNull(mythology, "mythology must not be null");
        this.gods = Objects.requireNonNull(gods, "gods must not be null");
        this.count = gods.size();
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.timestamp = Instant.now();
    }

    public String getMythology() {
        return mythology;
    }

    public List<String> getGods() {
        return gods;
    }

    public int getCount() {
        return count;
    }

    public String getSource() {
        return source;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GodsResponse that = (GodsResponse) obj;
        return count == that.count &&
                Objects.equals(mythology, that.mythology) &&
                Objects.equals(gods, that.gods) &&
                Objects.equals(source, that.source) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mythology, gods, count, source, timestamp);
    }

    @Override
    public String toString() {
        return "GodsResponse{" +
                "mythology='" + mythology + '\'' +
                ", gods=" + gods +
                ", count=" + count +
                ", source='" + source + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
