package info.jab.latency.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration test for GlobalExceptionHandler using a fake controller
 * that deliberately throws exceptions to test the exception handling flow.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @LocalServerPort
    private int port;

    @TestConfiguration
    static class FakeControllerConfiguration {

        @RestController
        static class FakeExceptionController {

            @GetMapping("/test/general-exception")
            public String throwGeneralException() throws Exception {
                throw new Exception("Test general exception for GlobalExceptionHandler");
            }

            @GetMapping("/test/null-pointer-exception")
            public String throwNullPointerException() {
                throw new NullPointerException("Test null pointer exception");
            }

            @GetMapping("/test/runtime-exception")
            public String throwRuntimeException() {
                throw new RuntimeException("Test runtime exception");
            }

            @GetMapping("/test/illegal-argument-exception")
            public String throwIllegalArgumentException() {
                throw new IllegalArgumentException("Test illegal argument exception");
            }

            @GetMapping("/test/illegal-state-exception")
            public String throwIllegalStateException() {
                throw new IllegalStateException("Test illegal state exception");
            }
        }
    }

    @Test
    void handleGeneralException_WhenExceptionThrown_ShouldReturn500() {
        given()
            .port(port)
        .when()
            .get("/test/general-exception")
        .then()
            .statusCode(500)
            .contentType("application/json")
            .body("error", equalTo("INTERNAL_SERVER_ERROR"))
            .body("message", equalTo("Unable to retrieve mythology data from external services"))
            .body("timestamp", notNullValue())
            .body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
    }

    @Test
    void handleGeneralException_WhenNullPointerExceptionThrown_ShouldReturn502() {
        // NullPointerException extends RuntimeException, so it's handled by RuntimeException handler
        given()
            .port(port)
        .when()
            .get("/test/null-pointer-exception")
        .then()
            .statusCode(502)
            .contentType("application/json")
            .body("error", equalTo("BAD_GATEWAY"))
            .body("message", equalTo("External mythology services are currently unavailable"))
            .body("timestamp", notNullValue());
    }

    @Test
    void handleRuntimeException_WhenRuntimeExceptionThrown_ShouldReturn502() {
        given()
            .port(port)
        .when()
            .get("/test/runtime-exception")
        .then()
            .statusCode(502)
            .contentType("application/json")
            .body("error", equalTo("BAD_GATEWAY"))
            .body("message", equalTo("External mythology services are currently unavailable"))
            .body("timestamp", notNullValue())
            .body("timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
    }

    @Test
    void handleRuntimeException_WhenIllegalArgumentExceptionThrown_ShouldReturn502() {
        given()
            .port(port)
        .when()
            .get("/test/illegal-argument-exception")
        .then()
            .statusCode(502)
            .contentType("application/json")
            .body("error", equalTo("BAD_GATEWAY"))
            .body("message", equalTo("External mythology services are currently unavailable"))
            .body("timestamp", notNullValue());
    }

    @Test
    void handleRuntimeException_WhenIllegalStateExceptionThrown_ShouldReturn502() {
        given()
            .port(port)
        .when()
            .get("/test/illegal-state-exception")
        .then()
            .statusCode(502)
            .contentType("application/json")
            .body("error", equalTo("BAD_GATEWAY"))
            .body("message", equalTo("External mythology services are currently unavailable"))
            .body("timestamp", notNullValue());
    }

    @Test
    void globalExceptionHandler_ShouldHandleExceptionsWithProperPriority() {
        // Test that RuntimeException handler takes precedence over general Exception handler
        // for IllegalArgumentException (which extends RuntimeException)
        given()
            .port(port)
        .when()
            .get("/test/illegal-argument-exception")
        .then()
            .statusCode(502) // Should be handled by RuntimeException handler, not general Exception handler
            .body("error", equalTo("BAD_GATEWAY"));
    }

    @Test
    void globalExceptionHandler_ShouldReturnConsistentErrorFormat() {
        // Verify all exception handlers return the same JSON structure
        given()
            .port(port)
        .when()
            .get("/test/general-exception")
        .then()
            .statusCode(500)
            .body("$", hasKey("error"))
            .body("$", hasKey("message"))
            .body("$", hasKey("timestamp"));

        given()
            .port(port)
        .when()
            .get("/test/runtime-exception")
        .then()
            .statusCode(502)
            .body("$", hasKey("error"))
            .body("$", hasKey("message"))
            .body("$", hasKey("timestamp"));
    }
}
