package info.jab.ms.config;

import java.net.http.HttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GreekGodsApiProperties.class)
public class RestClientConfig {

	static final String GREEK_GODS_JDK_REQUEST_FACTORY = "greekGodsJdkRequestFactory";

	@Bean(name = GREEK_GODS_JDK_REQUEST_FACTORY)
	public JdkClientHttpRequestFactory greekGodsJdkRequestFactory(GreekGodsApiProperties properties) {
		HttpClient httpClient =
				HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(properties.readTimeout());
		return requestFactory;
	}

	@Bean(name = "greekGodsRestClient")
	public RestClient greekGodsRestClient(
			GreekGodsApiProperties properties,
			@Qualifier(GREEK_GODS_JDK_REQUEST_FACTORY) JdkClientHttpRequestFactory requestFactory) {
		return RestClient.builder()
				.baseUrl(properties.baseUrl())
				.requestFactory(requestFactory)
				.build();
	}
}
