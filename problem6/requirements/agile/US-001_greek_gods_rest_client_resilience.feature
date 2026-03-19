Feature: Consume a REST Greek God Service with resilience policies
  As an API client
  I want rate limiting, retry, and circuit breaker behavior on HTTP calls
  So that I reliably obtain gods whose names start with "a" despite latency and faults

  Background:
    Given a REST API about Greek gods
    And the client is configured with a rate limit policy for HTTP traffic
    And the client is configured with a retry policy and a circuit breaker for outbound calls
    And connection timeouts are defined for every HTTP connection

  @acceptance-test
  Scenario: Consume the API in a happy path case
    When the client sends the request
    Then the response is successful
    And the result contains all gods whose names start with "a"

  @integration-test
  Scenario: Force an internal retry behaviour
    Given the first response is a transient failure
    When the client sends the request
    Then the retry policy is applied
    And the result contains all gods whose names start with "a"

  @integration-test
  Scenario: Consume the API with a bad response
    Given the service responds with an HTTP error or times out within configured limits
    When the client sends the request
    Then the retry policy is applied according to configuration
    And the result contains all gods whose names start with "a" once a successful response is obtained

  @integration-test
  Scenario: Consume the API with a corrupted response
    Given the service returns a body that is not valid for the expected Greek gods payload
    When the client sends the request
    Then the retry policy is applied where appropriate
    And the result contains all gods whose names start with "a" once a valid response is obtained

  @integration-test
  Scenario: Test a bad internal configuration
    Given the client has invalid or unsafe resilience configuration
    When the client sends the request
    Then the failure is handled predictably without undefined behaviour
    And the client does not bypass rate limit, retry, or circuit breaker guarantees
