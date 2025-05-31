package info.jab.latency.client;

/**
 * Client interface for fetching data from Wikipedia.
 */
public interface WikipediaClient {
    /**
     * Fetches the length of the content of a Wikipedia page.
     *
     * @param pageUrl The URL of the Wikipedia page.
     * @return The length of the page content. Returns 0 if the page cannot be fetched or an error occurs.
     */
    int fetchWikipediaPageLength(String pageUrl);
}
