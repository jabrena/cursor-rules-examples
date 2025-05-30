# Sequence Diagram: Multi-Mythology Gods Data Aggregation API Flow

## Overview
This sequence diagram illustrates the Multi-Mythology Gods Data Aggregation API process as described in the following agile artifacts:

- **Epic:** EPIC-004_Multi_Mythology_Gods_API.md
- **Feature:** FEAT-001_REST_API_Endpoint_Implementation.md
- **User Story:** US-001_REST_API_Gods_Endpoint.md
- **Acceptance Criteria:** rest_api_gods_endpoint.feature
- **API Specification:** mythology-aggregation-api.yaml
- **External API Specification:** my-json-server-oas.yaml

## Diagram Elements

### Actors/Participants
- **Client Application:** The consumer of the mythology aggregation API (mythology enthusiasts' applications)
- **Mythology Aggregation Service:** The internal REST API service that orchestrates data aggregation
- **Greek API:** External service providing Greek mythology gods data
- **Roman API:** External service providing Roman mythology gods data
- **Nordic API:** External service providing Nordic mythology gods data
- **Indian API:** External service providing Indian mythology gods data
- **Celtiberian API:** External service providing Celtiberian mythology gods data

### Key Interactions
1. **Initial Request:** Client calls GET `/api/v1/gods` to retrieve all mythology data
2. **Parallel External Calls:** Service simultaneously calls all 5 external mythology APIs
3. **Data Collection:** Service receives individual mythology arrays from each external API
4. **Data Aggregation:** Service transforms and unifies data into standardized format
5. **Response Delivery:** Service returns HTTP 200 with aggregated JSON response

## Process Flow Description

The sequence diagram shows a comprehensive flow starting with a client application requesting aggregated mythology data through the `/api/v1/gods` endpoint. Upon receiving the request, the Mythology Aggregation Service activates and initiates parallel calls to all five external mythology APIs (Greek, Roman, Nordic, Indian, and Celtiberian) using asynchronous messaging (represented by ->> arrows).

Each external API responds with an array of god names specific to their mythology. The aggregation service collects these responses and performs data transformation to create a unified format with unique IDs, mythology identifiers, and god names. The final response is returned to the client as HTTP 200 with a JSON array containing the standardized mythology data structure.

## Alternative Scenarios

### Error Handling
The diagram includes three main error scenarios:

1. **Gateway Timeout (HTTP 504):** When one or more external APIs fail to respond within the 5-second timeout limit
2. **Bad Gateway (HTTP 502):** When external services return error responses or are unavailable
3. **Internal Server Error (HTTP 500):** When internal processing errors occur during data aggregation

### Asynchronous Operations
All external API calls are performed asynchronously to optimize response time and allow parallel processing of multiple data sources. This approach ensures that the aggregation service can handle multiple external dependencies efficiently while maintaining the 5-second response time requirement.

## Technical Notes

### Technology Stack
- **Framework:** Spring Boot REST API implementation
- **Protocol:** HTTP/HTTPS for all API communications
- **Response Format:** JSON with standardized structure
- **External Services:** RESTful APIs hosted at my-json-server.typicode.com

### Data Flow
The system transforms individual mythology arrays into a unified structure:

**Input (from each external API):**
```json
["Zeus", "Hera", "Poseidon", ...]
```

**Output (aggregated response):**
```json
[
  {"id": 1, "mythology": "greek", "god": "Zeus"},
  {"id": 2, "mythology": "roman", "god": "Jupiter"},
  {"id": 3, "mythology": "nordic", "god": "Odin"}
]
```

### Performance Requirements
- **Response Time:** Must be less than 5 seconds for complete aggregation
- **Timeout Management:** 5-second timeout for external API calls
- **Parallel Processing:** Concurrent calls to all external services to minimize total response time

## Generating the Visual Diagram

### Using PlantUML Online
1. Copy the contents of `mythology_gods_aggregation_sequence.puml`
2. Go to http://www.plantuml.com/plantuml/uml/
3. Paste the code and generate the diagram

### Using PlantUML CLI
```bash
# Install PlantUML (requires Java)
java -jar plantuml.jar mythology_gods_aggregation_sequence.puml
```

### Using VS Code Extension
1. Install the "PlantUML" extension
2. Open the `.puml` file
3. Use Ctrl+Shift+P and select "PlantUML: Preview Current Diagram"

## Related Documentation
- [Epic: EPIC-004_Multi_Mythology_Gods_API.md](EPIC-004_Multi_Mythology_Gods_API.md)
- [Feature: FEAT-001_REST_API_Endpoint_Implementation.md](FEAT-001_REST_API_Endpoint_Implementation.md)
- [User Story: US-001_REST_API_Gods_Endpoint.md](US-001_REST_API_Gods_Endpoint.md)
- [Acceptance Criteria: rest_api_gods_endpoint.feature](rest_api_gods_endpoint.feature)
- [Internal API Specification: mythology-aggregation-api.yaml](mythology-aggregation-api.yaml)
- [External API Specification: my-json-server-oas.yaml](my-json-server-oas.yaml)

## Maintenance Notes
- Update this diagram when the underlying user stories or technical specifications change
- Consider creating separate diagrams for different error scenarios or system integrations
- Review and validate the diagram with stakeholders and technical teams
- Monitor external API availability and update error handling scenarios as needed
- Ensure the diagram reflects actual implementation patterns used in the Spring Boot service

## Implementation Considerations
- **Resilience Patterns:** Consider implementing circuit breaker patterns for external API calls
- **Caching Strategy:** Evaluate caching mechanisms to improve response times for frequently requested data
- **Monitoring:** Implement proper logging and metrics collection for external API interactions
- **Security:** Ensure secure communication with external services and proper input validation
- **Scalability:** Design the aggregation service to handle increased load and additional mythology sources
