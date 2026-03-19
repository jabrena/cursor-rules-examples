package info.jab.ms.acceptance;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import info.jab.ms.domain.GreekGod;
import info.jab.ms.BaseIntegrationTest;
import info.jab.ms.service.GreekGodsService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("acceptance-test")
class GreekGodsAcceptanceTest extends BaseIntegrationTest {

	@Autowired
	private GreekGodsService greekGodsService;

	@BeforeEach
	void stubHappyPath() {
		stubFor(
				get(urlPathEqualTo("/greek"))
						.willReturn(
								aResponse()
										.withHeader("Content-Type", "application/json")
										.withBodyFile(HAPPY_GODS_BODY_FILE)));
	}

	@Test
	@DisplayName("Consume the API in a happy path case")
	void happyPath_returnsGodsWhoseNamesStartWithA() {
		List<GreekGod> gods = greekGodsService.findGodsWhoseNamesStartWithA();
		assertThat(gods).isNotEmpty();
		assertThat(gods)
				.extracting(GreekGod::name)
				.containsExactlyInAnyOrderElementsOf(EXPECTED_GODS_NAMES_STARTING_WITH_A);
	}
}
