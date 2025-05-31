# Implement the acceptance test
_Exported on 30/5/2025 at 21:02:15 CEST from Cursor (0.50.7)_

---

**User**

implement the acceptance test

---

**Cursor**

```plaintext
Scenario: Successfully retrieve all mythology gods data
  Given the client application is ready to make API requests
  When the client sends a GET request to "/api/v1/gods"
  Then the response status should be 200
  And the response should contain JSON formatted mythology data
  And the response should include gods from all mythologies
  And the response time should be less than 5 seconds
  And the response should have the following structure:
    """
    [
        {
            "id": 1,
            "mythology": "greek",
            "god": "Zeus"
        },
        {
            "id": 2,
            "mythology": "roman",
            "god": "Jupiter"
        },
        {
            "id": 3,
            "mythology": "nordic",
            "god": "Odin"
        }
    ]
    """
```

I'll implement the acceptance test for the "Successfully retrieve all mythology gods data" scenario based on the requirements in the attached documents. Let me first explore the project structure and then implement the test.

Now I need to add the necessary dependencies for REST Assured and WireMock, and then create the acceptance test. Let me first update the pom.xml to include these dependencies, then create the test structure and implementation.

```xml
  <properties>
    <java.version>24</java.version>
    <maven.version>3.9.9</maven.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

    <!-- Dependencies -->
    <jspecify.version>1.0.0</jspecify.version>
    <spring-boot.version>3.4.1</spring-boot.version>

    <!-- Test dependencies -->
    <junit.version>5.12.0</junit.version>
    <mockito.version>5.18.0</mockito.version>
    <assertj.version>3.27.3</assertj.version>
    <rest-assured.version>5.5.0</rest-assured.version>
    <wiremock.version>3.11.3</wiremock.version>

// ... existing code ...
```

```xml
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
      <dependency>
        <groupId>org.junit</groupId>
        <artifactId>junit-bom</artifactId>
        <version>${junit.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
```

```xml
  <dependencies>
    <!-- Null Safety Annotations -->
    <dependency>
      <groupId>org.jspecify</groupId>
      <artifactId>jspecify</artifactId>
      <version>${jspecify.version}</version>
    </dependency>

    <!-- Spring Boot dependencies -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Test dependencies -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>

    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
      <scope>test</scope>
    </dependency>
    <!-- Optionally: parameterized tests support -->
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-params</artifactId>
      <scope>test</scope>
    </dependency>

    <!-- REST Assured for API testing -->
    <dependency>
      <groupId>io.rest-assured</groupId>
      <artifactId>rest-assured</artifactId>
      <version>${rest-assured.version}</version>
      <scope>test</scope>
    </dependency>

    <!-- WireMock for stubbing external services -->
    <dependency>
      <groupId>org.wiremock</groupId>
      <artifactId>wiremock-standalone</artifactId>
      <version>${wiremock.version}</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
```

Let me fix the WireMock dependency - it seems the artifact name might be incorrect. Let me check the correct artifact.

```xml
    <!-- WireMock for stubbing external services -->
    <dependency>
      <groupId>com.github.tomakehurst</groupId>
      <artifactId>wiremock-jre8</artifactId>
      <version>${wiremock.version}</version>
      <scope>test</scope>
    </dependency>
```

```xml
    <wiremock.version>2.35.2</wiremock.version>
```

Let me proceed with creating the test directory structure and the acceptance test. The dependency issue might resolve when we run Maven, and the important thing is to implement the test as requested.

Now, let me create the acceptance test file based on the Gherkin scenario and the ADR requirements:

```java
package info.jab.latency;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Acceptance test for the mythology gods endpoint.
 * 
 * This test implements the acceptance criteria from the Gherkin scenario:
 * "Successfully retrieve all mythology gods data"
 * 
 * Based on ADR-001 Acceptance Testing Strategy, this test uses:
 * - REST Assured for API testing
 * - WireMock for stubbing external mythology APIs
 * - Static test data based on OpenAPI specification examples
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MythologyGodsEndpointIT {

    @LocalServerPort
    private int port;

    private WireMockServer greekApiMock;
    private WireMockServer romanApiMock;
    private WireMockServer nordicApiMock;
    private WireMockServer indianApiMock;
    private WireMockServer celtiberianApiMock;

    @BeforeEach
    void setUp() {
        // Configure REST Assured
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // Set up WireMock servers for each external mythology API
        setupMockServers();
        stubExternalMythologyAPIs();
    }

    @AfterEach
    void tearDown() {
        // Stop all WireMock servers
        stopMockServers();
    }

    @Test
    void successfully_retrieve_all_mythology_gods_data() {
        // Given: The mythology aggregation service is running
        // And: All external mythology APIs are available (mocked)
        // And: The client application is ready to make API requests

        // When: The client sends a GET request to "/api/v1/gods"
        var response = given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: The response status should be 200
                .statusCode(200)
                // And: The response content type should be "application/json"
                .contentType(ContentType.JSON)
                // And: The response time should be less than 5 seconds
                .time(lessThan(5000L))
                // And: The response should contain JSON formatted mythology data
                .body("$", notNullValue())
                .body("size()", greaterThan(0))
                // And: Each god entry should contain required fields
                .body("id", everyItem(notNullValue()))
                .body("mythology", everyItem(notNullValue()))
                .body("god", everyItem(notNullValue()))
                // And: The response should include gods from all mythologies
                .body("mythology", hasItems("greek", "roman", "nordic", "indian", "celtiberian"))
                // And: The response should have the expected structure with specific gods
                .body("find { it.mythology == 'greek' }.god", equalTo("Zeus"))
                .body("find { it.mythology == 'roman' }.god", equalTo("Jupiter"))
                .body("find { it.mythology == 'nordic' }.god", equalTo("Odin"))
                .body("find { it.mythology == 'indian' }.god", equalTo("Brahma"))
                .body("find { it.mythology == 'celtiberian' }.god", equalTo("Ataecina"));

        // Additional validation: Verify the complete structure matches expected format
        response.body("$", hasSize(greaterThanOrEqualTo(5)));
    }

    private void setupMockServers() {
        greekApiMock = new WireMockServer(WireMockConfiguration.options().port(8081));
        romanApiMock = new WireMockServer(WireMockConfiguration.options().port(8082));
        nordicApiMock = new WireMockServer(WireMockConfiguration.options().port(8083));
        indianApiMock = new WireMockServer(WireMockConfiguration.options().port(8084));
        celtiberianApiMock = new WireMockServer(WireMockConfiguration.options().port(8085));

        greekApiMock.start();
        romanApiMock.start();
        nordicApiMock.start();
        indianApiMock.start();
        celtiberianApiMock.start();
    }

    private void stopMockServers() {
        if (greekApiMock != null) greekApiMock.stop();
        if (romanApiMock != null) romanApiMock.stop();
        if (nordicApiMock != null) nordicApiMock.stop();
        if (indianApiMock != null) indianApiMock.stop();
        if (celtiberianApiMock != null) celtiberianApiMock.stop();
    }

    private void stubExternalMythologyAPIs() {
        // Stub Greek mythology API
        greekApiMock.stubFor(get(urlEqualTo("/greek"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"Zeus\", \"Hera\", \"Poseidon\", \"Athena\"]")));

        // Stub Roman mythology API
        romanApiMock.stubFor(get(urlEqualTo("/roman"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"Jupiter\", \"Juno\", \"Neptune\", \"Minerva\"]")));

        // Stub Nordic mythology API
        nordicApiMock.stubFor(get(urlEqualTo("/nordic"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"Odin\", \"Thor\", \"Frigg\", \"Loki\"]")));

        // Stub Indian mythology API
        indianApiMock.stubFor(get(urlEqualTo("/indian"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"Brahma\", \"Vishnu\", \"Shiva\", \"Lakshmi\"]")));

        // Stub Celtiberian mythology API
        celtiberianApiMock.stubFor(get(urlEqualTo("/celtiberian"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[\"Ataecina\", \"Endovelicus\", \"Bandua\", \"Reue\"]")));
    }
}
```

Let me fix the REST Assured syntax errors:

```java
    @Test
    void successfully_retrieve_all_mythology_gods_data() {
        // Given: The mythology aggregation service is running
        // And: All external mythology APIs are available (mocked)
        // And: The client application is ready to make API requests

        // When: The client sends a GET request to "/api/v1/gods"
        given()
            .when()
                .get("/api/v1/gods")
            .then()
                // Then: The response status should be 200
                .statusCode(200)
                // And: The response content type should be "application/json"
                .contentType(ContentType.JSON)
                // And: The response time should be less than 5 seconds
                .time(lessThan(5000L))
                // And: The response should contain JSON formatted mythology data
                .body("$", notNullValue())
                .body("size()", greaterThan(0))
                // And: Each god entry should contain required fields
                .body("id", everyItem(notNullValue()))
                .body("mythology", everyItem(notNullValue()))
                .body("god", everyItem(notNullValue()))
                // And: The response should include gods from all mythologies
                .body("mythology", hasItems("greek", "roman", "nordic", "indian", "celtiberian"))
                // And: The response should have the expected structure with specific gods
                .body("find { it.mythology == 'greek' }.god", equalTo("Zeus"))
                .body("find { it.mythology == 'roman' }.god", equalTo("Jupiter"))
                .body("find { it.mythology == 'nordic' }.god", equalTo("Odin"))
                .body("find { it.mythology == 'indian' }.god", equalTo("Brahma"))
                .body("find { it.mythology == 'celtiberian' }.god", equalTo("Ataecina"))
                // Additional validation: Verify the complete structure matches expected format
                .body("$", hasSize(greaterThanOrEqualTo(5)));
    }
```

Let me check the specific syntax issue and fix it. The problem seems to be with the Groovy-style JSON path expressions:

```java
                // And: The response should include gods from all mythologies
                .body("mythology", hasItems("greek", "roman", "nordic", "indian", "celtiberian"))
                // And: The response should have the expected structure with specific gods
                .body("findAll { it.mythology == 'greek' }.god", hasItem("Zeus"))
                .body("findAll { it.mythology == 'roman' }.god", hasItem("Jupiter"))
                .body("findAll { it.mythology == 'nordic' }.god", hasItem("Odin"))
                .body("findAll { it.mythology == 'indian' }.god", hasItem("Brahma"))
                .body("findAll { it.mythology == 'celtiberian' }.god", hasItem("Ataecina"))
```

Now let me also create a test resources directory and add a test configuration to support the Spring Boot application context for the integration test. I'll create a minimal application class for testing purposes:

```yaml
server:
  port: 0

spring:
  main:
    web-application-type: servlet

# External mythology API URLs for integration with WireMock
mythology:
  apis:
    greek: http://localhost:8081/greek
    roman: http://localhost:8082/roman
    nordic: http://localhost:8083/nordic
    indian: http://localhost:8084/indian
    celtiberian: http://localhost:8085/celtiberian

logging:
  level:
    info.jab.latency: DEBUG
    com.github.tomakehurst.wiremock: WARN
```

```java
package info.jab.latency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot application for the mythology aggregation service.
 * This serves as the main entry point for the application and will be used
 * by the integration tests to start the application context.
 */
@SpringBootApplication
public class MythologyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MythologyApplication.class, args);
    }
}
```

Now let me run the test to verify it's set up correctly and will fail as expected (since there's no implementation yet):

I see the issue - there's a compatibility problem with the WireMock version and Jakarta servlets. Let me fix the dependency to use the correct version:

```xml
    <!-- WireMock for stubbing external services -->
    <dependency>
      <groupId>com.github.tomakehurst</groupId>
      <artifactId>wiremock-standalone</artifactId>
      <version>${wiremock.version}</version>
      <scope>test</scope>
    </dependency>
```

Let me use the correct artifact. It seems there might be an issue with the versions. Let me try a different approach and use WireMock with Spring Boot compatibility:

```xml
    <wiremock.version>3.10.0</wiremock.version>
```

```xml
    <!-- WireMock for stubbing external services -->
    <dependency>
      <groupId>org.wiremock</groupId>
      <artifactId>wiremock-standalone</artifactId>
      <version>${wiremock.version}</version>
      <scope>test</scope>
    </dependency>
```

Let me try running the test with the current configuration to see if Maven can resolve the dependencies at runtime, despite the IDE linter error:

Great! The compilation works. Now let me run the integration test to see if it fails as expected:

Perfect! The acceptance test is working exactly as expected. It's failing with a 404 status code, which means:

1. ✅ The Spring Boot application is starting correctly
2. ✅ The WireMock servers are being set up successfully 
3. ✅ The REST Assured test is making the HTTP request to `/api/v1/gods`
4. ✅ The test is failing because there's no implementation yet (404 Not Found)

This is exactly what we wanted - the acceptance test is properly implemented and will fail until the actual implementation is created.

## Summary

I have successfully implemented the acceptance test for the "Successfully retrieve all mythology gods data" scenario in the `info.jab.latency` package as requested. Here's what was accomplished:

### 📁 **Files Created/Modified:**

1. **`pom.xml`** - Added necessary dependencies:
   - Spring Boot starter web and test
   - REST Assured for API testing
   - WireMock for stubbing external services

2. **`src/main/java/info/jab/latency/MythologyApplication.java`** - Created minimal Spring Boot application class

3. **`src/test/java/info/jab/latency/MythologyGodsEndpointIT.java`** - Main acceptance test implementation

4. **`src/test/resources/application-test.yml`** - Test configuration

### 🧪 **Test Implementation Details:**

The acceptance test follows the **ADR-001 Acceptance Testing Strategy** and implements the Gherkin scenario:

- **Framework**: REST Assured with Spring Boot Test
- **Mocking**: WireMock for stubbing all 5 external mythology APIs (Greek, Roman, Nordic, Indian, Celtiberian)
- **Validation**: Complete API contract validation including:
  - HTTP 200 status code
  - JSON content type
  - Response time < 5 seconds
  - Required fields (id, mythology, god)
  - Data from all 5 mythologies
  - Specific god names as per OpenAPI spec

### ✅ **Test Status:**

The test **fails as expected** with a 404 status code because:
- No REST controller implementation exists yet
- No `/api/v1/gods` endpoint has been implemented
- This is the correct behavior for outside-in TDD

### 🎯 **Next Steps:**

The test is now ready to guide the implementation phase. When you implement the actual REST API endpoint and service layer, this test will validate that the implementation meets all the acceptance criteria from the Gherkin scenario.

The test covers all the requirements from the attached documents:
- User Story US-001 requirements
- OpenAPI specification compliance  
- Performance requirements (< 5 seconds)
- Data structure validation
- External API integration patterns

