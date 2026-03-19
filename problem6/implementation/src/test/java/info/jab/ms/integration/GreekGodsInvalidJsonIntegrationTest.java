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
class GreekGodsInvalidJsonIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private GreekGodsService greekGodsService;

	@BeforeEach
	void stub() {
		stubFor(
				get(urlPathEqualTo("/greek"))
						.inScenario("bad-json")
						.whenScenarioStateIs(STARTED)
						.willReturn(
								aResponse()
										.withHeader("Content-Type", "application/json")
										.withBody("not-valid-json{"))
						.willSetStateTo("ok"));
		stubFor(
				get(urlPathEqualTo("/greek"))
						.inScenario("bad-json")
						.whenScenarioStateIs("ok")
						.willReturn(
								aResponse()
										.withHeader("Content-Type", "application/json")
										.withBodyFile(HAPPY_GODS_BODY_FILE)));
	}

	@Test
	@DisplayName("Invalid JSON then valid payload")
	void thenSuccess() {
		assertThat(greekGodsService.findGodsWhoseNamesStartWithA())
				.extracting(GreekGod::name)
				.containsExactlyInAnyOrderElementsOf(EXPECTED_GODS_NAMES_STARTING_WITH_A);
	}
}
