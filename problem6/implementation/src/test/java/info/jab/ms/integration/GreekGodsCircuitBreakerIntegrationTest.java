package info.jab.ms.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import info.jab.ms.BaseIntegrationTest;
import info.jab.ms.service.GreekGodsService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@Tag("integration-test")
@TestPropertySource(
		properties = {
			"resilience4j.circuitbreaker.instances.greekGods.sliding-window-size=2",
			"resilience4j.circuitbreaker.instances.greekGods.minimum-number-of-calls=2",
			"resilience4j.circuitbreaker.instances.greekGods.failure-rate-threshold=50",
			"resilience4j.circuitbreaker.instances.greekGods.wait-duration-in-open-state=10s",
			"resilience4j.retry.instances.greekGods.max-attempts=1"
		})
class GreekGodsCircuitBreakerIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private GreekGodsService greekGodsService;

	@Autowired
	private CircuitBreakerRegistry circuitBreakerRegistry;

	@BeforeEach
	void stubRepeated503() {
		stubFor(
				get(urlPathEqualTo("/greek"))
						.willReturn(
								aResponse()
										.withStatus(503)
										.withHeader("Content-Type", "text/plain")));
	}

	@Test
	@DisplayName("Repeated failures open the circuit; responses stay empty")
	void circuitOpens() {
		assertThat(greekGodsService.findGodsWhoseNamesStartWithA()).isEmpty();
		assertThat(greekGodsService.findGodsWhoseNamesStartWithA()).isEmpty();

		CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("greekGods");
		assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

		assertThat(greekGodsService.findGodsWhoseNamesStartWithA()).isEmpty();
	}
}
