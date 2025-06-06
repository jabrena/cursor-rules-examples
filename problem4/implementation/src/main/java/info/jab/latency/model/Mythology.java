package info.jab.latency.model;

/**
 * Enumeration of supported mythologies.
 *
 * Each mythology corresponds to an external API endpoint that provides
 * god names for that particular mythology.
 */
public enum Mythology {
    GREEK("greek"),
    ROMAN("roman"),
    NORDIC("nordic"),
    INDIAN("indian"),
    CELTIBERIAN("celtiberian");

    private final String name;

    Mythology(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
