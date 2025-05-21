package info.jab.latency;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

public interface LatencyService {

    CompletableFuture<BigInteger> calculateSumForGodsStartingWith(String prefix);

}
