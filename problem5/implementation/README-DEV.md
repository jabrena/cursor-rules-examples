# Greek Gods API - Development Guide

```bash
# Run the application with Docker Compose (PostgreSQL will start automatically)
./mvnw clean spring-boot:run
# Swagger UI: http://localhost:8080/swagger-ui.html
# API Docs: http://localhost:8080/api-docs

./mvnw clean verify
./mvnw clean verify jacoco:report -Pjacoco
jwebserver -p 8004 -d "$(pwd)/target/site/jacoco"
```

