package info.jab.latency.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Map;

/**
 * Global exception handler for the mythology aggregation API.
 *
 * Provides centralized error handling as specified in ADR-001.
 * Returns consistent error responses following the OpenAPI specification.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles general exceptions and returns a 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception ex, WebRequest request) {

        logger.error("Internal server error occurred", ex);

        Map<String, Object> errorResponse = Map.of(
            "error", "INTERNAL_SERVER_ERROR",
            "message", "Unable to retrieve mythology data from external services",
            "timestamp", Instant.now().toString()
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }

    /**
     * Handles runtime exceptions and returns a 502 Bad Gateway.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        logger.error("Runtime exception occurred", ex);

        Map<String, Object> errorResponse = Map.of(
            "error", "BAD_GATEWAY",
            "message", "External mythology services are currently unavailable",
            "timestamp", Instant.now().toString()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(errorResponse);
    }
}
