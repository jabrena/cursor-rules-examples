# Greek Gods API - Development Guide

## Running with Docker Compose (Local Development)

To run the application with an automatically managed PostgreSQL database using Docker Compose:

```bash
# Run the application with Docker Compose (PostgreSQL will start automatically)
./mvnw clean spring-boot:run
```

## Other Development Commands

### Running tests with coverage
```bash
./mvnw clean verify jacoco:report -Pjacoco
jwebserver -p 8004 -d "$(pwd)/target/site/jacoco"
```

## API Documentation

Once the application is running, you can access:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs
