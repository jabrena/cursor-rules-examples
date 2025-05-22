package info.jab.latency;

import java.util.List;

/**
 * Defines the contract for a service that identifies the Greek god(s)
 * with the most literature on Wikipedia, based on character count of their pages.
 */
public interface GreekGodsLiteratureAnalyzer {

    /**
     * Analyzes Wikipedia pages for a list of Greek gods to find who has the most literature.
     *
     * The process involves:
     * 1. Fetching a list of Greek gods from the provided API URL.
     * 2. For each god, constructing their Wikipedia URL using the provided template.
     * 3. Fetching the content of each Wikipedia page.
     * 4. Calculating the character length of the content (0 if a page is not retrieved).
     * 5. Identifying the god(s) with the maximum character length.
     *
     * @param apiEndpoints A list of strings containing the necessary API URLs.
     *                     It's expected that:
     *                     - The first element (index 0) is the URL for the Greek Gods API
     *                       (e.g., "https://my-json-server.typicode.com/jabrena/latency-problems/greek").
     *                     - The second element (index 1) is the Wikipedia URL template
     *                       (e.g., "https://en.wikipedia.org/wiki/{greekGod}").
     * @return A list of strings, where each string contains the name of a god
     *         (or gods, in case of a tie) who has the most literature,
     *         along with their character count. For example: ["Zeus (15023 characters)", "Hera (15023 characters)"].
     *         Returns an empty list if no gods can be processed or an error occurs that prevents analysis.
     */
    List<String> solve(List<String> apiEndpoints);
}
