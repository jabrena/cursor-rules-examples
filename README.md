# Cursor Rules Examples

## Motivation

Probe that using **Cursor rules for Agile & Java** are useful when the Software Engineer have to face non trivial Java problems.

## Getting started

### 1. Review the Latency problems

- https://github.com/jabrena/latency-problems

### 2. Intall the cursor rules in your workspace

```bash
sdk install jbang
jbang cache clear
jbang catalog list jabrena
jbang setup@jabrena init --cursor agile
jbang setup@jabrena init --cursor java
```

### 3. Generate the software requirements based on the initial problem description

```bash
Create an agile development checklist using @2000-agile-checklist
```

### 4.Implement the solution

- 4.1 Setup the Maven project

```bash
jbang setup@jabrena init --maven
```

**Note:** Remove the `pluginManagement` section in the the pom.xml because the default one is so much verbose.

```bash
Update the cursor rule using @101-java-maven-deps-and-plugins
Verify the changes with ./mvnw clean verify
```

- 4.2 Implement the Acceptance test ([Outside-in TDD London](https://outsidein.dev/concepts/outside-in-tdd/)) based on the Gherkin file

![](./docs/double-loop-tdd.png)

**Note:** Attach the User story & Gherkin file from requirements folder

```bash
Implement an acceptance tests in the package info.jab.latency for the scenario:
"Successfully retrieve all mythology gods data".
Don´t develop any source code, only implement the acceptance test.
It will fail because in this phase, doesn´t exist any implemention.
```

**Note:** if the REST/Cli development need to interact with a External third party service, it could be possible that the acceptance tests Stub the external integration also.

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

- 4.3 Implement the solution to pass the acceptance tests

```bash
Implement a solution in the package info.jab.latency from src.
Create a solution and later add test classes.
Dont´t change the acceptance tests.
Verify the changes with the command: ./mvnw clean verify
```

```bash
./mvnw clean verify surefire-report:report
jwebserver -p 8001 -d "$(pwd)/target/reports"
./mvnw clean verify jacoco:report -Pjacoco
jwebserver -p 8002 -d "$(pwd)/target/site/jacoco"
./mvnw clean verify -Dmaven.build.cache.enabled=false
```

- 4.4 Implement the tests

```bash
Review the coverage with ./mvnw clean verify jacoco:report -Pjacoco and increase the coverage in instructions, classes & branches until 80% @problem4
```

- 4.5 Refactor the initial stable solution

```bash
No recipe, this is the added value of a good SSE. ¯\_(ツ)_/¯
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
- https://github.com/jabrena/latency-problems
- https://github.com/jabrena/101-cursor
- https://github.com/jabrena/setup-cli
- https://github.com/jabrena/jbang-catalog
