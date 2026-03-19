package info.jab.ms.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "greek-gods.api")
public record GreekGodsApiProperties(
		@NotBlank String baseUrl,
		@NotNull Duration connectTimeout,
		@NotNull Duration readTimeout) {}
