# Cursor Rules Examples

## Motivation

Probe the help of the **Cursor rules for Java** with non trivial Java problems.

## Getting started

1. [x] Review requirements.

Have a conversation with the LLM to improve the initial requirements.

```bash
How to improve this gherkin? What questions do you consider useful to be anwered in order to update this gherkin file?
```

2. [x] Create a Maven project

Create a Maven project to solve the problem

```bash
jbang setup@jabrena init --maven
```

3. [x] Develop an acceptance test

Review if the solution require some initial intefaces to be used for the tests.

```bash
implement an acceptance tests in the package info.jab.latency for the scenario: "Identify the Greek god with the most literature on Wikipedia". Use the open api to extract the examples to the tests using Wiremock and run the tests against an empty implementation of the interface.
```

```xml
<properties>
    <wiremock.version>3.13.0</wiremock.version>
</properties>

<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>${wiremock.version}</version>
    <scope>test</scope>
</dependency>
```

4. [ ] Implement the solution

Implement the solution.

```bash
implement a solution in the package info.jab.latency from src. Create a solution and later add test classes. Verify the changes with the command: ./mvnw clean verify
```

4. Begin the refactoring

- Improve the design
- Simplify the solution desing for the main class, use a interface
- Improve pom.xml
- Improve the tests
- Migrate to assertj
- Extract samples from oas d

## How to test in local?

```bash
./mvnw clean verify
```

## References

- https://github.com/jabrena/cursor-rules-java
- https://github.com/jabrena/latency-problems
- https://github.com/jabrena/latency-rosetta-stone
- https://editor-next.swagger.io/
