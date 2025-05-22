package info.jab.latency;

import java.util.Collections;
import java.util.List;

/**
 * Default empty implementation of GreekGodsLiteratureAnalyzer.
 * This implementation is intended to be filled out to solve the problem.
 */
public class DefaultGreekGodsLiteratureAnalyzer implements GreekGodsLiteratureAnalyzer {

    @Override
    public List<String> solve(List<String> apiEndpoints) {
        // This is an empty implementation.
        // The actual logic will fetch gods from apiEndpoints.get(0)
        // and their Wikipedia page lengths from apiEndpoints.get(1).
        return Collections.emptyList();
    }
}
