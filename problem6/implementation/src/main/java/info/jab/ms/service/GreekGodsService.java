package info.jab.ms.service;

import info.jab.ms.domain.GreekGod;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GreekGodsService {

	private static final Logger log = LoggerFactory.getLogger(GreekGodsService.class);

	private final RestClient greekGodsRestClient;

	public GreekGodsService(@Qualifier("greekGodsRestClient") RestClient greekGodsRestClient) {
		this.greekGodsRestClient = greekGodsRestClient;
	}

	@Retry(name = "greekGods", fallbackMethod = "emptyGodsFallback")
	@CircuitBreaker(name = "greekGods")
	@RateLimiter(name = "greekGods")
	public List<GreekGod> findGodsWhoseNamesStartWithA() {
		List<GreekGod> all =
				greekGodsRestClient
						.get()
						.uri("/greek")
						.retrieve()
						.body(new ParameterizedTypeReference<>() {});
		return namesStartingWithA(copyFromUpstream(all));
	}

	/** Defensive copy of the raw upstream list (empty if null or empty). */
	private List<GreekGod> copyFromUpstream(List<GreekGod> raw) {
		if (raw == null || raw.isEmpty()) {
			return List.of();
		}
		return List.copyOf(raw);
	}

	/** Gods whose {@link GreekGod#name()} is non-null and starts with {@code "a"} (case-insensitive). */
	private List<GreekGod> namesStartingWithA(List<GreekGod> gods) {
		return gods.stream()
				.filter(g -> g.name() != null)
				.filter(g -> g.name().toLowerCase(Locale.ROOT).startsWith("a"))
				.toList();
	}

	@SuppressWarnings("unused")
	private List<GreekGod> emptyGodsFallback(Throwable throwable) {
		log.warn(
				"Greek gods upstream unavailable after resilience policies; returning empty list",
				throwable);
		return List.of();
	}
}
