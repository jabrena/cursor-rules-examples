package info.jab.ms.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;

import info.jab.ms.BaseIntegrationTest;
import info.jab.ms.domain.GreekGod;
import info.jab.ms.service.GreekGodsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("integration-test")
class GreekGodsTimeoutIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private GreekGodsService greekGodsService;

	@BeforeEach
	void stubSlowThenFast() {
		stubFor(
				get(urlPathEqualTo("/greek"))
						.inScenario("slow-fast")
						.whenScenarioStateIs(STARTED)
						.willReturn(
								aResponse()
										.withHeader("Content-Type", "application/json")
										.withFixedDelay(12_000)
										.withBodyFile(HAPPY_GODS_BODY_FILE))
						.willSetStateTo("fast"));
		stubFor(
				get(urlPathEqualTo("/greek"))
						.inScenario("slow-fast")
						.whenScenarioStateIs("fast")
						.willReturn(
								aResponse()
										.withHeader("Content-Type", "application/json")
										.withBodyFile(HAPPY_GODS_BODY_FILE)));
	}

	@Test
	@DisplayName("Read timeout then fast response: retry succeeds")
	void timeoutThenSuccess() {
		assertThat(greekGodsService.findGodsWhoseNamesStartWithA())
				.extracting(GreekGod::name)
				.containsExactlyInAnyOrderElementsOf(EXPECTED_GODS_NAMES_STARTING_WITH_A);
	}
}
