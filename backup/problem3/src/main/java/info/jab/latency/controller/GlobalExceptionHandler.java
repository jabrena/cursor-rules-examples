package info.jab.latency.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import info.jab.latency.dto.ErrorResponse;
import info.jab.latency.service.MythologyService;

/**
 * Global exception handler for the God Information Gateway API.
 *
 * Handles low-level exceptions not managed in regular way and converts them to appropriate HTTP responses.
 * Provides consistent error response format across all API endpoints.
 *
 * Based on C4 Component diagram: GodGateway_Component.puml
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles invalid mythology parameter exceptions.
     *
     * @param ex The IllegalArgumentException thrown for invalid mythology parameters
     * @param request The web request that caused the exception
     * @return ResponseEntity with error response and 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMythologyParameter(
            IllegalArgumentException ex, WebRequest request) {

        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid mythology parameter";
        logger.warn("Invalid mythology parameter: {}", message);

        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_MYTHOLOGY",
                message,
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles mythology service exceptions (external API failures).
     *
     * @param ex The MythologyServiceException thrown when external API calls fail
     * @param request The web request that caused the exception
     * @return ResponseEntity with error response and 503 status
     */
    @ExceptionHandler(MythologyService.MythologyServiceException.class)
    public ResponseEntity<ErrorResponse> handleMythologyServiceException(
            MythologyService.MythologyServiceException ex, WebRequest request) {

        logger.error("Mythology service exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                "SERVICE_UNAVAILABLE",
                "The mythology service is currently unavailable. Please try again later.",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Handles 404 Not Found exceptions.
     *
     * @param ex The NoResourceFoundException
     * @param request The web request
     * @return ResponseEntity with error response and 404 status
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            NoResourceFoundException ex, WebRequest request) {

        logger.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "RESOURCE_NOT_FOUND",
                "The requested resource was not found.",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles all other unexpected exceptions.
     *
     * @param ex The general exception
     * @param request The web request that caused the exception
     * @return ResponseEntity with error response and 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {

        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact support if the problem persists.",
                getPath(request)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Extracts the request path from the WebRequest.
     *
     * @param request The web request
     * @return The request path or a default value if not available
     */
    private String getPath(WebRequest request) {
        String path = request.getDescription(false);
        if (path.startsWith("uri=")) {
            return path.substring(4);
        }
        return path;
    }
}
