# Multi-Mythology Gods API Sequence Diagram

This document describes the sequence flow for the Multi-Mythology Gods Data Aggregation API, which retrieves and aggregates god data from multiple mythology sources.

## System Overview

The API aggregates mythology data from five different sources:
- Greek Gods API
- Roman Gods API
- Nordic Gods API
- Indian Gods API
- Celtiberian Gods API

## Sequence Diagram

```mermaid
sequenceDiagram
    participant Client as Client Application
    participant Controller as REST API Controller<br/>(/api/v1/gods)
    participant Aggregator as Data Aggregation Service
    participant APIClient as External API Client
    participant GreekAPI as Greek Gods API<br/>(my-json-server)
    participant RomanAPI as Roman Gods API<br/>(my-json-server)
    participant NordicAPI as Nordic Gods API<br/>(my-json-server)
    participant IndianAPI as Indian Gods API<br/>(my-json-server)
    participant CeltibrianAPI as Celtiberian Gods API<br/>(my-json-server)

    rect rgb(173, 216, 230)
        Note over Controller, APIClient: Internal System
    end

    rect rgb(255, 255, 224)
        Note over GreekAPI, CeltibrianAPI: External Services
    end

    Client->>+Controller: GET /api/v1/gods
    Note right of Controller: Handle incoming request<br/>for aggregated mythology data

    Controller->>+Aggregator: aggregateAllMythologyGods()
    Note right of Aggregator: Coordinate data retrieval<br/>from all mythology sources

    par Greek Mythology
        Aggregator->>+APIClient: getGreekGods()
        APIClient->>+GreekAPI: GET /greek
        GreekAPI-->>-APIClient: ["Zeus", "Hera", "Poseidon", ...]
        APIClient-->>-Aggregator: Greek gods list
    and Roman Mythology
        Aggregator->>+APIClient: getRomanGods()
        APIClient->>+RomanAPI: GET /roman
        RomanAPI-->>-APIClient: ["Venus", "Mars", "Neptun", ...]
        APIClient-->>-Aggregator: Roman gods list
    and Nordic Mythology
        Aggregator->>+APIClient: getNordicGods()
        APIClient->>+NordicAPI: GET /nordic
        NordicAPI-->>-APIClient: ["Baldur", "Freyja", "Heimdall", ...]
        APIClient-->>-Aggregator: Nordic gods list
    and Indian Mythology
        Aggregator->>+APIClient: getIndianGods()
        APIClient->>+IndianAPI: GET /indian
        IndianAPI-->>-APIClient: ["Brahma", "Vishnu", "Shiva", ...]
        APIClient-->>-Aggregator: Indian gods list
    and Celtiberian Mythology
        Aggregator->>+APIClient: getCeltibrianGods()
        APIClient->>+CeltibrianAPI: GET /celtiberian
        CeltibrianAPI-->>-APIClient: ["Ataecina", "Candamius", ...]
        APIClient-->>-Aggregator: Celtiberian gods list
    end

    Note over Aggregator: Transform and merge data<br/>into unified format with<br/>id, mythology, god fields

    Aggregator-->>-Controller: List<MythologyGod>
    Controller-->>-Client: HTTP 200 OK<br/>JSON: [{"id":1,"mythology":"greek","god":"Zeus"}...]

    Note right of Client: Response time < 5 seconds<br/>All mythology data aggregated<br/>in single response
```

## Error Handling Scenarios

### Timeout Scenario

```mermaid
sequenceDiagram
    participant APIClient as External API Client
    participant GreekAPI as Greek Gods API
    participant Aggregator as Data Aggregation Service
    participant Controller as REST API Controller
    participant Client as Client Application

    APIClient->>+GreekAPI: GET /greek (timeout)
    GreekAPI-->>-APIClient: 504 Gateway Timeout
    APIClient-->>Aggregator: Timeout exception
    Aggregator-->>Controller: Partial data or error
    Controller-->>Client: HTTP 504 Gateway Timeout
    Note right of Controller: Handle timeout gracefully<br/>Return appropriate error response
```

### Service Unavailable Scenario

```mermaid
sequenceDiagram
    participant APIClient as External API Client
    participant RomanAPI as Roman Gods API
    participant Aggregator as Data Aggregation Service
    participant Controller as REST API Controller
    participant Client as Client Application

    APIClient->>+RomanAPI: GET /roman
    RomanAPI-->>-APIClient: 500 Internal Server Error
    APIClient-->>Aggregator: Service unavailable exception
    Aggregator-->>Controller: Error response
    Controller-->>Client: HTTP 502 Bad Gateway
    Note right of Controller: External service unavailable<br/>Return appropriate error code
```

## Key Features

- **Parallel Processing**: All external API calls are made concurrently to minimize response time
- **Data Transformation**: Raw API responses are transformed into a unified format
- **Error Handling**: Graceful handling of timeouts and service unavailability
- **Performance**: Target response time under 5 seconds
- **Unified Response**: Single JSON response containing all mythology data

## Response Format

The API returns a unified JSON structure:

```json
[
  {
    "id": 1,
    "mythology": "greek",
    "god": "Zeus"
  },
  {
    "id": 2,
    "mythology": "roman",
    "god": "Venus"
  }
]
```
