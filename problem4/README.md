# Cursor Rules Examples

## Motivation

Probe the help of the **Cursor rules for Java** with non trivial Java problems.

## Getting started

```bash
sdk install jbang
jbang cache clear
jbang catalog list jabrena
jbang setup@jabrena init --cursor agile
jbang setup@jabrena init --cursor java

Create an agile development checklist using @2000-agile-checklist
```

## Development

- 2. [x] Create a Maven project

  - 2.1. [x] Create a Maven project to solve the problem

```bash
jbang setup@jabrena init --maven
```


**Note:** Remove the `pluginManagement` section in the the pom.xml because the default one is so much verbose.


  - 2.2 [x] Update the pom.xml

```bash
Update the cursor rule using @101-java-maven-deps-and-plugins
Verify the changes with ./mvnw clean verify
```

3. [x] Develop an acceptance test

Review if the solution require some initial intefaces to be used for the tests.

![](./docs/double-loop-tdd.png)

- 3.1 [x] Add acceptance tests

**Note:** Attach the User story, Gherkin file

```bash
Implement an acceptance tests in the package info.jab.latency for the scenario:
"Successfully retrieve all mythology gods data".
Don´t develop any source code, only implement the acceptance test.
It will fail because in this phase, doesn´t exist any implemention.
```

- **Notes:**Review that the implementation use RestAssured.
- **Notes2:**Review that the implementation not use Cucumber.

4. [x] Implement the solution

Implement the solution.

```bash
Implement a solution in the package info.jab.latency from src.
Create a solution and later add test classes.
Dont´t change the acceptance tests.
Verify the changes with the command: ./mvnw clean verify
```

```bash
Update the acceptance tests pointing to the real endpoints.
Verify the changes with the command: ./mvnw clean verify
```

./mvnw clean verify -Dmaven.build.cache.enabled=false

```xml
<properties>
    <wiremock.version>3.13.0</wiremock.version>
    <jackson.version>2.17.0</jackson.version>
</properties>

<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>${wiremock.version}</version>
    <scope>test</scope>
</dependency>
```

4. [x] Refactoring

Improve the running solution

- 4.1 [x] Improve the design

- 4.4 [x] Improve the tests

```bash
Improve the tests@test verify the changes with ./mvnw clean test
```

## How to test in local?

```bash
./mvnw clean verify
```

## References

- https://editor-next.swagger.io/
- https://www.plantuml.com/plantuml/uml/
- https://cekrem.github.io/posts/double-loop-tdd-blog-engine-pt2/
- ...
- https://github.com/jabrena/cursor-rules-methodology
- https://github.com/jabrena/cursor-rules-agile
- https://github.com/jabrena/cursor-rules-tasks
- https://github.com/jabrena/cursor-rules-java
- https://github.com/jabrena/cursor-rules-examples
- https://github.com/jabrena/101-cursor
- https://github.com/jabrena/setup-cli
- https://github.com/jabrena/jbang-catalog
