# Cursor Rules Examples

## Motivation

Probe the help of the **Cursor rules for Java** with non trivial Java problems.

## Getting started

1. Maven project creation

```bash
jbang setup@jabrena init --maven
```

2. Have a conversation with the LLM to improve the initial requirements:

```bash
how to improve this gherkin? What questions do you consider useful to be anwered in order to update this gherkin file?
```

3. Ask for an initial implementation

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
- https://editor-next.swagger.io/