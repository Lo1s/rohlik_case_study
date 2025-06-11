# Rohlik Case Study – E-commerce Spring Boot Application

This project is a sample e-commerce backend built with Java, Spring Boot, and H2 in-memory database. It provides REST APIs for managing products and orders, including stock reservation, order expiration, and payment workflows.

## Features

- Product catalog management (CRUD)
- Order creation, payment, and cancellation
- Stock reservation and release on order expiration/cancellation
- Scheduled job to expire unpaid orders
- H2 in-memory database with web console
- API documentation via Swagger UI
- Unit and integration tests

## Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/com/rohlik/case_study/         # Java source code
│   │   └── resources/                          # Application config & static files
│   └── test/java/com/rohlik/case_study/        # Tests
├── public/                                     # Frontend static pages (dev server)
├── dev-server.js                               # Node.js dev proxy for API mocking
├── pom.xml                                     # Maven build file
├── mvnw, mvnw.cmd                              # Maven wrapper scripts
└── ...
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+ (or use included Maven Wrapper)

### Running the Application

**With Maven Wrapper (recommended):**
```sh
./mvnw spring-boot:run
```

**Or build and run the JAR:**
```sh
./mvnw clean package
java -jar target/case_study-0.0.1-SNAPSHOT.jar
```

The API will be available at [http://localhost:8080](http://localhost:8080).

### API Documentation

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### H2 Database Console

- [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- JDBC URL: `jdbc:h2:mem:testdb`

### Running Tests

```sh
./mvnw test
```

## Development Proxy

A Node.js dev server is available for API mocking and static frontend:
```sh
npm install
npm run dev
```
- Dev server: [http://localhost:3000](http://localhost:3000)

## Useful Commands

- **Build:** `./mvnw clean package`
- **Run:** `./mvnw spring-boot:run`
- **Test:** `./mvnw test`
- **Clean:** `./mvnw clean`

**Author:** Jiri Slapnicka