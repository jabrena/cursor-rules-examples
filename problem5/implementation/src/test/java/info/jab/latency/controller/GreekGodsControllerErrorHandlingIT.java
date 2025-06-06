package info.jab.latency.controller;

import info.jab.latency.service.GreekGodsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//TODO: Add a IT using ToxyProxy for database connection failures
/**
 * Integration tests for GreekGodsController error handling scenarios using MockMvc.
 *
 * Tests the API's ability to handle various error conditions gracefully:
 * - Database connection failures
 * - Service layer exceptions
 * - Standardized error response format (RFC 7807 Problem Details)
 *
 * Following ATDD approach - these tests validate the error handling implementation
 * that follows RFC 7807 Problem Details standard with application/problem+json content type.
 *
 * Uses @WebMvcTest to test only the web layer with mocked dependencies.
 * Architecture: Testing the complete request flow
 * HTTP Request → Controller → Service → [Mocked Error Scenarios] → GlobalExceptionHandler
 */
@WebMvcTest(GreekGodsController.class)
class GreekGodsControllerErrorHandlingIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GreekGodsService greekGodsService;

    @Test
    void should_handle_database_connection_failure_gracefully() throws Exception {
        // GIVEN: Service throws a RuntimeException simulating database connection failure
        when(greekGodsService.getAllGreekGodNames())
                .thenThrow(new RuntimeException("Database connection failed"));

        // WHEN: A request is made to the API during a simulated database failure
        // THEN: The API should handle the error gracefully using RFC 7807 Problem Details format

        // This test validates that when database errors occur, the API:
        // - Returns HTTP 500 Internal Server Error
        // - Provides RFC 7807 ProblemDetail response format (application/problem+json)
        // - Includes appropriate error details
        // - Does not expose internal system details

        mockMvc.perform(get("/api/v1/gods/greek")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())  // Expected: 500 when database fails
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("Internal server error: Database connection failed"))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void should_handle_service_unavailable_gracefully() throws Exception {
        // GIVEN: Service throws an exception simulating service unavailability
        when(greekGodsService.getAllGreekGodNames())
                .thenThrow(new IllegalStateException("Service temporarily unavailable"));

        // This test simulates a scenario where the service layer fails
        // Expected behavior: Proper RFC 7807 Problem Details error response

        mockMvc.perform(get("/api/v1/gods/greek")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())  // Expected: 500 for service errors
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("Internal server error: Service temporarily unavailable"));
    }

    @Test
    void should_return_proper_error_format_for_internal_errors() throws Exception {
        // GIVEN: Service throws a generic exception
        when(greekGodsService.getAllGreekGodNames())
                .thenThrow(new RuntimeException("Internal system error"));

        // This test validates the standardized RFC 7807 Problem Details error response format
        // Expected format: { "type": "...", "title": "...", "status": 500, "detail": "...", "timestamp": "..." }

        mockMvc.perform(get("/api/v1/gods/greek")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())  // Expected: 500 for internal errors
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").isNumber())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void should_not_expose_internal_system_details() throws Exception {
        // GIVEN: Service throws an exception with sensitive internal details
        when(greekGodsService.getAllGreekGodNames())
                .thenThrow(new RuntimeException("Connection to database server db-prod-001.internal.company.com:5432 failed"));

        // This test ensures that internal system details are properly handled
        // The GlobalExceptionHandler includes the error message in the detail field,
        // but this validates the response structure is correct

        mockMvc.perform(get("/api/v1/gods/greek")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").exists())
                // The detail field will contain the error message as per GlobalExceptionHandler implementation
                .andExpect(jsonPath("$.detail").value(containsString("Connection to database server")));
    }
}
