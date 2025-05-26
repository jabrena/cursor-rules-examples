# God Information Gateway Service - Sequence Diagram

This document describes the sequence flow for the God Information Gateway Service, which provides access to mythology data from multiple external sources through a unified API.

## Epic and User Story Context

- **Epic:** EPIC-001 - God Information Gateway API
- **Feature:** FEAT-001 - God Information Gateway API
- **User Story:** US-001 - Basic God Information Service

## System Overview

The God Information Gateway Service acts as a proxy/gateway to aggregate mythology data from five external APIs:
- Greek Gods API
- Roman Gods API
- Nordic Gods API
- Indian Gods API
- Celtiberian Gods API

All external APIs are hosted at: `https://my-json-server.typicode.com/jabrena/latency-problems/`

## Main Success Scenario: Retrieve Greek Gods

```mermaid
sequenceDiagram
    participant Client as API Consumer
    participant Gateway as God Gateway API<br/>(localhost:8080)
    participant Greek as Greek Gods API<br/>(external)

    Note over Client, Greek: Epic: EPIC-001 - God Information Gateway API<br/>Feature: FEAT-001 - God Information Gateway API<br/>User Story: US-001 - Basic God Information Service

    Note over Gateway: God Information Gateway API<br/>is running on localhost:8080

    Note over Client, Greek: Scenario: Successfully retrieve Greek gods list<br/>Given: All external mythology APIs are available

    Client->>+Gateway: GET /api/v1/gods/greek

    Note over Gateway: Validate mythology parameter<br/>(accepts: greek, roman, nordic, indian, celtiberian<br/>and uppercase for backward compatibility)

    Gateway->>+Greek: GET /greek
    Note over Greek: External API at<br/>my-json-server.typicode.com/<br/>jabrena/latency-problems/greek
    Greek-->>-Gateway: Response: ["Zeus", "Hera", "Poseidon", ...]

    Note over Gateway: Process and format response<br/>with metadata (count, source, timestamp)

    Gateway-->>-Client: HTTP 200 OK<br/>{"mythology": "greek",<br/>"gods": ["Zeus", "Hera", "Poseidon", ...],<br/>"count": 20,<br/>"source": "external_api",<br/>"timestamp": "2024-01-15T10:30:00Z"}

    Note over Client: Acceptance Criteria Met:<br/>✓ HTTP 200 status received<br/>✓ Response contains Greek gods including<br/>  "Zeus", "Hera", "Poseidon"<br/>✓ Response mythology field is lowercase
```

## Backward Compatibility: Uppercase Parameters

```mermaid
sequenceDiagram
    participant Client as API Consumer
    participant Gateway as God Gateway API<br/>(localhost:8080)
    participant Greek as Greek Gods API<br/>(external)

    Client->>+Gateway: GET /api/v1/gods/GREEK

    Note over Gateway: Accepts uppercase for backward compatibility<br/>Converts internally to lowercase

    Gateway->>+Greek: GET /greek
    Greek-->>-Gateway: Response: ["Zeus", "Hera", "Poseidon", ...]

    Gateway-->>-Client: HTTP 200 OK<br/>{"mythology": "greek",<br/>"gods": ["Zeus", "Hera", "Poseidon", ...],<br/>"count": 20,<br/>"source": "external_api",<br/>"timestamp": "2024-01-15T10:30:00Z"}

    Note over Client: Note: Response always returns<br/>lowercase mythology name
```

## Alternative Flows: Other Mythologies

```mermaid
sequenceDiagram
    participant Client as API Consumer
    participant Gateway as God Gateway API<br/>(localhost:8080)
    participant Roman as Roman Gods API<br/>(external)
    participant Nordic as Nordic Gods API<br/>(external)
    participant Indian as Indian Gods API<br/>(external)
    participant Celtiberian as Celtiberian Gods API<br/>(external)

    Client->>+Gateway: GET /api/v1/gods/roman
    Gateway->>+Roman: GET /roman
    Roman-->>-Gateway: ["Venus", "Mars", "Neptun", ...]
    Gateway-->>-Client: HTTP 200 OK with Roman gods<br/>(mythology: "roman")

    Client->>+Gateway: GET /api/v1/gods/nordic
    Gateway->>+Nordic: GET /nordic
    Nordic-->>-Gateway: ["Baldur", "Freyja", "Heimdall", ...]
    Gateway-->>-Client: HTTP 200 OK with Nordic gods<br/>(mythology: "nordic")

    Client->>+Gateway: GET /api/v1/gods/indian
    Gateway->>+Indian: GET /indian
    Indian-->>-Gateway: ["Brahma", "Vishnu", "Shiva", ...]
    Gateway-->>-Client: HTTP 200 OK with Indian gods<br/>(mythology: "indian")

    Client->>+Gateway: GET /api/v1/gods/celtiberian
    Gateway->>+Celtiberian: GET /celtiberian
    Celtiberian-->>-Gateway: ["Ataecina", "Candamius", ...]
    Gateway-->>-Client: HTTP 200 OK with Celtiberian gods<br/>(mythology: "celtiberian")
```

## Error Scenarios

### Invalid Mythology Parameter

```mermaid
sequenceDiagram
    participant Client as API Consumer
    participant Gateway as God Gateway API<br/>(localhost:8080)

    Client->>+Gateway: GET /api/v1/gods/INVALID
    Note over Gateway: Invalid mythology parameter
    Gateway-->>-Client: HTTP 400 Bad Request<br/>{"error": "INVALID_MYTHOLOGY",<br/>"message": "Valid values are: greek, roman, nordic, indian, celtiberian"}
```

### Timeout Scenario

```mermaid
sequenceDiagram
    participant Client as API Consumer
    participant Gateway as God Gateway API<br/>(localhost:8080)
    participant Greek as Greek Gods API<br/>(external)

    Client->>+Gateway: GET /api/v1/gods/greek
    Gateway->>+Greek: GET /greek
    Note over Greek: Timeout after 10 seconds
    Greek-xGateway: Timeout
    Gateway-->>-Client: HTTP 504 Gateway Timeout<br/>{"error": "GATEWAY_TIMEOUT"}
```

### Service Unavailable Scenario

```mermaid
sequenceDiagram
    participant Client as API Consumer
    participant Gateway as God Gateway API<br/>(localhost:8080)
    participant Greek as Greek Gods API<br/>(external)

    Client->>+Gateway: GET /api/v1/gods/greek
    Gateway->>+Greek: GET /greek
    Note over Greek: Service unavailable
    Greek-xGateway: Service unavailable
    Gateway-->>-Client: HTTP 503 Service Unavailable<br/>{"error": "SERVICE_UNAVAILABLE"}
```

## Key Implementation Details

- **Gateway Architecture**: Aggregates from 5 external APIs
- **Timeout Configuration**: 10-second timeout for all external connections
- **External API Base URL**: `https://my-json-server.typicode.com/jabrena/latency-problems/`
- **Technology Stack**: Spring Boot microservice architecture
- **Concurrency**: Thread-safe concurrent access support
- **Backward Compatibility**: Accepts both lowercase and uppercase mythology parameters

## Supported Mythologies

| Parameter | External Endpoint | Example Gods |
|-----------|------------------|--------------|
| `greek` | `/greek` | Zeus, Hera, Poseidon |
| `roman` | `/roman` | Venus, Mars, Neptun |
| `nordic` | `/nordic` | Baldur, Freyja, Heimdall |
| `indian` | `/indian` | Brahma, Vishnu, Shiva |
| `celtiberian` | `/celtiberian` | Ataecina, Candamius |

## Response Format

### Success Response

```json
{
  "mythology": "greek",
  "gods": ["Zeus", "Hera", "Poseidon", "..."],
  "count": 20,
  "source": "external_api",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Error Responses

#### Invalid Mythology (400 Bad Request)
```json
{
  "error": "INVALID_MYTHOLOGY",
  "message": "Valid values are: greek, roman, nordic, indian, celtiberian"
}
```

#### Gateway Timeout (504)
```json
{
  "error": "GATEWAY_TIMEOUT"
}
```

#### Service Unavailable (503)
```json
{
  "error": "SERVICE_UNAVAILABLE"
}
```

## Acceptance Criteria

- ✅ HTTP 200 status received for valid requests
- ✅ Response contains expected gods for each mythology
- ✅ Response mythology field is always lowercase
- ✅ Backward compatibility with uppercase parameters
- ✅ Proper error handling for invalid parameters
- ✅ Timeout handling for slow external services
- ✅ Service unavailable handling for failed external services
