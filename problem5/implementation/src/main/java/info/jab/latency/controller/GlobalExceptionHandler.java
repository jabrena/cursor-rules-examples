package info.jab.latency.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.URI;

/**
 * Global Exception Handler for the Greek Gods API.
 *
 * Implements centralized error handling using @ControllerAdvice annotation.
 * Uses RFC 7807 ProblemDetail standard for consistent error response format.
 * Includes comprehensive logging for error scenarios and debugging.
 *
 * This handler supports the acceptance criteria:
 * - AC3: Proper HTTP status codes (200, 500)
 * - Standardized error response format following RFC 7807
 * - Graceful handling of runtime exceptions
 * - Comprehensive logging for debugging and monitoring
 *
 * Error Response Format (RFC 7807 ProblemDetail):
 * {
 *   "type": "https://greekgods.api/problems/internal-server-error",
 *   "title": "Internal Server Error",
 *   "status": 500,
 *   "detail": "Specific error message description",
 *   "instance": "/api/v1/gods/greek"
 * }
 *
 * Architecture: Cross-cutting concern in the C4 Component Model
 * Intercepts exceptions from Controller → Service → Repository layers
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles generic runtime exceptions as a fallback.
     *
     * This method catches any uncaught RuntimeException that might occur in:
     * - Service layer business logic
     * - Controller layer processing
     * - Background synchronization services
     * - Database connection failures
     * - Data access errors
     *
     * @param ex The RuntimeException that was thrown
     * @param request The web request context
     * @return ResponseEntity with RFC 7807 ProblemDetail format and HTTP 500 status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        // Log error details for debugging and monitoring
        logger.error("Runtime exception occurred during request processing. " +
                    "Request: {}, Exception type: {}, Message: {}",
                    request.getDescription(false),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error: " + ex.getMessage()
        );

        problemDetail.setType(URI.create("https://greekgods.api/problems/internal-server-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", java.time.Instant.now());

        // Log the response being sent back
        logger.debug("Returning ProblemDetail response: status={}, detail={}",
                    problemDetail.getStatus(),
                    problemDetail.getDetail());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    /**
     * Handles all other unspecified exceptions as a catch-all.
     *
     * This method provides a safety net for any unexpected exceptions
     * that don't match the more specific exception handlers above.
     *
     * @param ex The generic Exception that was thrown
     * @param request The web request context
     * @return ResponseEntity with RFC 7807 ProblemDetail format and HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, WebRequest request) {

        // Log unexpected error details for debugging and monitoring
        logger.error("Unexpected exception caught by global handler. " +
                    "Request: {}, Exception type: {}, Message: {}",
                    request.getDescription(false),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Unexpected error occurred: " + ex.getMessage()
        );

        problemDetail.setType(URI.create("https://greekgods.api/problems/unexpected-error"));
        problemDetail.setTitle("Unexpected Error");
        problemDetail.setProperty("timestamp", java.time.Instant.now());

        // Log the response being sent back
        logger.debug("Returning ProblemDetail response: status={}, detail={}",
                    problemDetail.getStatus(),
                    problemDetail.getDetail());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    /**
     * Handles HTTP method not supported exceptions (405 Method Not Allowed).
     *
     * This occurs when clients attempt to use unsupported HTTP methods
     * on valid endpoints (e.g., POST/PUT/DELETE on GET-only endpoints).
     *
     * @param ex The HttpRequestMethodNotSupportedException that was thrown
     * @param request The web request context
     * @return ResponseEntity with RFC 7807 ProblemDetail format and HTTP 405 status
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {

        // Log method not supported details for debugging and monitoring
        logger.warn("HTTP method not supported. " +
                   "Request: {}, Method: {}, Supported methods: {}",
                   request.getDescription(false),
                   ex.getMethod(),
                   String.join(", ", ex.getSupportedMethods()));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.METHOD_NOT_ALLOWED,
            "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint"
        );

        problemDetail.setType(URI.create("https://greekgods.api/problems/method-not-allowed"));
        problemDetail.setTitle("Method Not Allowed");
        problemDetail.setProperty("method", ex.getMethod());
        problemDetail.setProperty("supportedMethods", ex.getSupportedMethods());
        problemDetail.setProperty("timestamp", java.time.Instant.now());

        // Log the response being sent back
        logger.debug("Returning ProblemDetail response: status={}, detail={}",
                    problemDetail.getStatus(),
                    problemDetail.getDetail());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(problemDetail);
    }

    /**
     * Handles requests to non-existent endpoints (404 Not Found).
     *
     * This occurs when clients make requests to URLs that don't match any
     * controller mapping in the application.
     *
     * @param ex The NoHandlerFoundException that was thrown
     * @param request The web request context
     * @return ResponseEntity with RFC 7807 ProblemDetail format and HTTP 404 status
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoHandlerFound(
            NoHandlerFoundException ex, WebRequest request) {

        // Log the unmapped request for monitoring and debugging
        logger.warn("No handler found for request. " +
                   "Method: {}, URL: {}, Request: {}",
                   ex.getHttpMethod(),
                   ex.getRequestURL(),
                   request.getDescription(false));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "No endpoint found for " + ex.getHttpMethod() + " " + ex.getRequestURL()
        );

        problemDetail.setType(URI.create("https://greekgods.api/problems/endpoint-not-found"));
        problemDetail.setTitle("Endpoint Not Found");
        problemDetail.setProperty("method", ex.getHttpMethod());
        problemDetail.setProperty("requestUrl", ex.getRequestURL());
        problemDetail.setProperty("timestamp", java.time.Instant.now());

        // Log the response being sent back
        logger.debug("Returning ProblemDetail response: status={}, detail={}",
                    problemDetail.getStatus(),
                    problemDetail.getDetail());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
}
