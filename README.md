# Cursor Rules Examples

## Motivation

Probe the help of the **Cursor rules for Java** with non trivial Java problems.

## Getting started

- 1. [x] Review requirements.

  - 1.1 [x] Create an `Epic` about the development

**Note:** Attach the initial free format text/markdown document describing the problem to solve.

```bash
Create an agile epic based the initial documentation received and use @2001-agile-create-an-epic
```

  - 1.2 [x] Create a `Feature` about the development

**Note:** Attach the EPIC created previously

```bash
Create a feature based on the epic and use @2002-agile-create-features-from-epics
```

**Note:** Review if the rule generates several features and maybe it is possible to merge into a single one. If you prefer to have only one feature, ask it.

  - 1.3 [x] Create an `User story` and the `Acceptance criteria` in `Gherkin` format based on the requirements.

**Note:** Attach the EPIC and the Feature created previously

```bash
Create a user story based on the feature and the acceptance criteria using the information provided with the cursor rule @2003-agile-create-user-stories
```

  - 1.4 [x] Create an `UML` Sequence diagrama about the functional requirements

**Note:** Attach the EPIC,Feature,User Story & Gherkin created previously

```bash
Create the UML sequence diagram based in plantuml format using the information provided with the cursor rule @2004-uml-sequence-diagram-from-agile-artifacts
```

  - 1.5 [x] Create the `C4 Model` diagrams based on the requirements

**Note:** Attach the EPIC,Feature,User Story, Gherkin & UML Sequence diagram created previously

```bash
Create the C4 Model diagrams from the requirements in plantuml format using the information provided with the cursor rule @2005-c4-diagrams-about-solution
```

**Note:** Review the diagrams, sometimes it is necessary to simplify the models.

  - 1.6 [x] Create an `ADR` about the functional requirements

**Note:** Attach the EPIC,Feature,User Story, Gherkin, UML Sequence diagram & C4 Model diagrams created previously

**Terminal/Cli development:**

```bash
Create the ADR about functional requirements using the cursor rule @2006-adr-create-functional-requirements-for-cli-development
```

**REST API development:**

```bash
Create the ADR about the functional requirements using the information provided with the cursor rule @2006-adr-create-functional-requirements-for-rest-api-development
```

  - 1.7 [x] Create an `ADR` about the acceptance testing Strategy

**Note:** Attach User Story & Gherkin created previously

```bash
Create the ADR about the acceptance testing strategy using the information provided with the cursor rule @2007-adr-create-acceptance-testing-strategy
```

  - 1.8 [x] Create an `ADR` about the non functional requirements

**Note:** Attach the EPIC,Feature,User Story, Gherkin, UML Sequence diagram & C4 Model diagrams created previously

```bash
Create the ADR about the functional requirements using the information provided with the cursor rule @2008-adr-create-non-functional-requirements-decisions
```

--

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

```bash
Implement an acceptance tests in the package info.jab.latency for the scenario:
"Successfully retrieve Greek gods list".
Don´t develop any source code, only implement the acceptance test.
It will fail
```

- **Notes:**Review that the implementation use RestAssured.
- **Notes2:**Review that the implementation not use Cucumber.

- 1.2 [x] Improve pom.xml

- **Notes:** Review that plugins are not located in pluginManagement

4. [x] Implement the solution

Implement the solution.

```bash
Implement a solution in the package info.jab.latency from src.
Create a solution and later add test classes.
Dont´t change the acceptance tests.
Don´t change the Interface defined in package info.jab.latency
Verify the changes with the command: ./mvnw clean verify
```

```bash
Update the acceptance tests pointing to the real endpoints.
Verify the changes with the command: ./mvnw clean verify
```

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
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>${jackson.version}</version>
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
