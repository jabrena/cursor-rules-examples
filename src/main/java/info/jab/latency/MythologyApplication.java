package info.jab.latency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot application for the mythology aggregation service.
 * This serves as the main entry point for the application and will be used
 * by the integration tests to start the application context.
 */
@SpringBootApplication
public class MythologyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MythologyApplication.class, args);
    }
}
