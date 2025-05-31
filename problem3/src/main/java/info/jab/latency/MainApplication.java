package info.jab.latency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the God Information Gateway API.
 *
 * This Spring Boot application provides a unified REST API for accessing god information
 * from multiple mythology sources (Greek, Roman, Nordic, Indian, Celtiberian).
 *
 * Epic: EPIC-001 - God Information Gateway API
 * Feature: FEAT-001 - God Information Gateway API
 * User Story: US-001 - Basic God Information Service
 */
@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
