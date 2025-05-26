# Sequence Diagram: Multi-Mythology Gods Data Aggregation API Flow

## Overview
This sequence diagram illustrates the Multi-Mythology Gods Data Aggregation API process as described in the following agile artifacts:

- **Epic:** EPIC-004_Multi_Mythology_Gods_API.md
- **Feature:** FEAT-001_REST_API_Endpoint_Implementation.md
- **User Story:** US-001_REST_API_Gods_Endpoint.md
- **API Specification:** mythology-aggregation-api.yaml
- **External API Specification:** my-json-server-oas.yaml
- **Acceptance Criteria:** rest_api_gods_endpoint.feature

## Diagram Elements

### Actors/Participants
- **Client Application:** The external application or user making requests to the mythology API
- **REST API Controller:** The Spring Boot REST controller handling the `/api/v1/gods` endpoint
- **Data Aggregation Service:** Internal service responsible for coordinating data retrieval and transformation
- **External API Client:** Component responsible for making HTTP calls to external mythology APIs
- **Greek Gods API:** External service providing Greek mythology data
- **Roman Gods API:** External service providing Roman mythology data
- **Nordic Gods API:** External service providing Nordic mythology data
- **Indian Gods API:** External service providing Indian mythology data
- **Celtiberian Gods API:** External service providing Celtiberian mythology data

### Key Interactions
1. **Client Request:** Client sends GET request to `/api/v1/gods` endpoint
2. **Service Coordination:** Controller delegates to aggregation service for data collection
3. **Parallel API Calls:** Simultaneous requests to all 5 external mythology APIs
4. **Data Transformation:** Raw mythology data is transformed into unified format
5. **Response Delivery:** Aggregated data returned as JSON with HTTP 200 status

## Process Flow Description

The sequence diagram shows the complete flow for retrieving aggregated mythology data:

1. **Initial Request:** A client application sends a GET request to the `/api/v1/gods` endpoint
2. **Request Handling:** The REST API Controller receives the request and activates the Data Aggregation Service
3. **Parallel Data Retrieval:** The aggregation service coordinates parallel calls to all five external mythology APIs:
   - Greek Gods API (`/greek`)
   - Roman Gods API (`/roman`)
   - Nordic Gods API (`/nordic`)
   - Indian Gods API (`/indian`)
   - Celtiberian Gods API (`/celtiberian`)
4. **Data Collection:** Each external API returns its respective mythology data as string arrays
5. **Data Transformation:** The aggregation service transforms the collected data into a unified format with `id`, `mythology`, and `god` fields
6. **Response Formation:** The transformed data is returned to the controller as a `List<MythologyGod>`
7. **Client Response:** The controller returns HTTP 200 OK with the aggregated JSON data to the client

## Alternative Scenarios

### Error Handling
The diagram includes two primary error scenarios:

1. **External API Timeout (504):** When an external API doesn't respond within the timeout period, the system returns a 504 Gateway Timeout error
2. **External API Unavailable (502):** When an external API returns a 500 Internal Server Error, the system responds with a 502 Bad Gateway error

### Asynchronous Operations
The diagram uses PlantUML's `par/else/end` construct to show that the five external API calls are made in parallel, optimizing response time and system efficiency.

## Technical Notes

### Technology Stack
- **Framework:** Spring Boot REST API
- **External APIs:** JSON-based REST services hosted on my-json-server.typicode.com
- **Response Format:** JSON with structured mythology data
- **HTTP Methods:** GET requests for data retrieval
- **Error Handling:** Standard HTTP status codes (200, 502, 504)

### Data Flow
- **Input:** HTTP GET request to `/api/v1/gods`
- **External Data:** String arrays from each mythology API
- **Transformation:** Raw strings converted to structured objects with:
  - `id`: Unique identifier (integer)
  - `mythology`: Source mythology (string: "greek", "roman", "nordic", "indian", "celtiberian")
  - `god`: God name (string)
- **Output:** JSON array of `MythologyGod` objects

### Performance Requirements
- **Response Time:** Must be less than 5 seconds for complete aggregation
- **Parallel Processing:** All external API calls executed simultaneously
- **Timeout Handling:** Graceful degradation when external services are slow or unavailable

## Generating the Visual Diagram

### Using PlantUML Online
1. Copy the contents of `mythology_gods_api_sequence.puml`
2. Go to http://www.plantuml.com/plantuml/uml/
3. Paste the code and generate the diagram

### Using PlantUML CLI
```bash
# Install PlantUML (requires Java)
java -jar plantuml.jar mythology_gods_api_sequence.puml
```

### Using VS Code Extension
1. Install the "PlantUML" extension
2. Open the `.puml` file
3. Use Ctrl+Shift+P and select "PlantUML: Preview Current Diagram"

## Related Documentation
- [Epic: EPIC-004_Multi_Mythology_Gods_API.md](EPIC-004_Multi_Mythology_Gods_API.md)
- [Feature: FEAT-001_REST_API_Endpoint_Implementation.md](FEAT-001_REST_API_Endpoint_Implementation.md)
- [User Story: US-001_REST_API_Gods_Endpoint.md](US-001_REST_API_Gods_Endpoint.md)
- [API Specification: mythology-aggregation-api.yaml](mythology-aggregation-api.yaml)
- [External API Specification: my-json-server-oas.yaml](my-json-server-oas.yaml)
- [Acceptance Criteria: rest_api_gods_endpoint.feature](rest_api_gods_endpoint.feature)

## Maintenance Notes
- Update this diagram when the underlying user stories or technical specifications change
- Consider creating separate diagrams for different error scenarios or alternative flows
- Review and validate the diagram with stakeholders and technical teams
- Keep the diagram synchronized with actual implementation details
- Update external API endpoints if the my-json-server.typicode.com structure changes

## Implementation Considerations
- **Resilience:** Implement circuit breaker patterns for external API calls
- **Caching:** Consider caching external API responses to improve performance
- **Monitoring:** Add logging and metrics for each external API call
- **Configuration:** Make external API URLs configurable for different environments
- **Testing:** Create integration tests that mock external API responses
