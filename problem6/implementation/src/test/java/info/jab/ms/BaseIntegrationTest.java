package info.jab.ms;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.ToxiproxyContainer.ContainerProxy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Shared WireMock + Toxiproxy Testcontainers stack for integration tests.
 *
 * <p>Maven Surefire is configured with {@code reuseForks=false} so each test class runs in a fresh
 * JVM: static Testcontainers and Spring contexts do not leak between classes (no ordering rules).
 * WireMock mappings and the circuit breaker are still reset between test methods in a class.
 * {@link DynamicPropertySource} uses a lazy supplier so each context reads the current toxiproxy port.
 *
 * <p>Happy-path JSON is served via WireMock {@code withBodyFile(...)}: the classpath resource
 * {@code __files/greek-gods-happy.json} is copied into the container under
 * {@code /home/wiremock/__files/} (WireMock standalone layout).
 * The payload mirrors
 * the real {@code /greek} list from {@code my-json-server.typicode.com/jabrena/latency-problems},
 * using {@code {"name":...}} objects so Jackson matches {@code GreekGod} (the live API returns bare
 * strings; the same names are represented as objects here).
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(
		properties = {
			// Avoid false positives when many tests share one context (default limit 10/s).
			"resilience4j.ratelimiter.instances.greekGods.limit-for-period=100000",
			"resilience4j.ratelimiter.instances.greekGods.limit-refresh-period=1s"
		})
public abstract class BaseIntegrationTest {

	@Autowired
	private CircuitBreakerRegistry circuitBreakerRegistry;

	/**
	 * Classpath resource under {@code src/test/resources/__files/greek-gods-happy.json}.
	 */
	private static final String HAPPY_GODS_CLASSPATH = "__files/greek-gods-happy.json";

	/**
	 * File name under WireMock's {@code __files} directory after copy. Subclasses can use
	 * {@code aResponse().withBodyFile(HAPPY_GODS_BODY_FILE)} for the same payload.
	 */
	protected static final String HAPPY_GODS_BODY_FILE = "greek-gods-happy.json";

	/**
	 * Names starting with {@code "a"} (case-insensitive) from the full {@link #HAPPY_GODS_BODY_FILE}
	 * dataset — use with {@code findGodsWhoseNamesStartWithA()} assertions.
	 */
	protected static final List<String> EXPECTED_GODS_NAMES_STARTING_WITH_A =
			List.of("Aphrodite", "Apollo", "Ares", "Artemis", "Athena");

	private static final String WIREMOCK_HOME = "/home/wiremock";

	private static final Network NETWORK = Network.newNetwork();

	@Container
	static final GenericContainer<?> wiremock =
			new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:3.10.0"))
					.withNetwork(NETWORK)
					.withNetworkAliases("wiremock")
					.withExposedPorts(8080)
					.withCopyFileToContainer(
							MountableFile.forClasspathResource(HAPPY_GODS_CLASSPATH),
							WIREMOCK_HOME + "/__files/" + HAPPY_GODS_BODY_FILE)
					.waitingFor(Wait.forHttp("/__admin/mappings").forPort(8080).forStatusCode(200));

	/** Uses Testcontainers default image {@code ghcr.io/shopify/toxiproxy:2.1.0}. */
	@Container
	static final ToxiproxyContainer toxiproxy =
			new ToxiproxyContainer().withNetwork(NETWORK).dependsOn(wiremock);

	@DynamicPropertySource
	static void registerGreekGodsBaseUrl(DynamicPropertyRegistry registry) {
		registry.add(
				"greek-gods.api.base-url",
				() -> {
					ContainerProxy proxy = toxiproxy.getProxy(wiremock, 8080);
					return "http://" + toxiproxy.getHost() + ":" + proxy.getProxyPort();
				});
	}

	@BeforeEach
	void resetWireMockBetweenTests() {
		resetWireMock();
		circuitBreakerRegistry.circuitBreaker("greekGods").reset();
	}

	/** Clears WireMock mappings; subclasses typically add stubs in their own {@code @BeforeEach}. */
	protected void resetWireMock() {
		WireMock.configureFor(wiremock.getHost(), wiremock.getMappedPort(8080));
		WireMock.resetToDefault();
	}
}
