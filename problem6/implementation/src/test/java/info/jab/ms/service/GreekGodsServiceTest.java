package info.jab.ms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import info.jab.ms.domain.GreekGod;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class GreekGodsServiceTest {

	@Mock
	private RestClient restClient;

	@Mock
	private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

	@Mock
	private RestClient.ResponseSpec responseSpec;

	private GreekGodsService greekGodsService;

	@BeforeEach
	void setUp() {
		greekGodsService = new GreekGodsService(restClient);
	}

	@Test
	@DisplayName("Returns only gods whose names start with 'a' (case-insensitive)")
	void filtersNamesStartingWithA() {
		stubRetrieveBody(
				List.of(
						new GreekGod("Apollo"),
						new GreekGod("Athena"),
						new GreekGod("Zeus"),
						new GreekGod("ares")));

		List<GreekGod> result = greekGodsService.findGodsWhoseNamesStartWithA();

		assertThat(result)
				.extracting(GreekGod::name)
				.containsExactlyInAnyOrder("Apollo", "Athena", "ares");
	}

	@Test
	@DisplayName("Returns empty list when upstream returns null body")
	void emptyWhenBodyNull() {
		stubRetrieveBody(null);

		assertThat(greekGodsService.findGodsWhoseNamesStartWithA()).isEmpty();
	}

	@Test
	@DisplayName("Returns empty list when upstream returns empty list")
	void emptyWhenBodyEmpty() {
		stubRetrieveBody(List.of());

		assertThat(greekGodsService.findGodsWhoseNamesStartWithA()).isEmpty();
	}

	@Test
	@DisplayName("Propagates RestClientException without Spring AOP (plain unit test)")
	void propagatesRestClientExceptionWithoutProxy() {
		when(restClient.get()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(eq("/greek"))).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.body(any(ParameterizedTypeReference.class)))
				.thenThrow(new org.springframework.web.client.ResourceAccessException("boom"));

		assertThrows(
				org.springframework.web.client.ResourceAccessException.class,
				() -> greekGodsService.findGodsWhoseNamesStartWithA());
	}

	@SuppressWarnings("unchecked")
	private void stubRetrieveBody(List<GreekGod> body) {
		when(restClient.get()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(eq("/greek"))).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(body);
	}
}
