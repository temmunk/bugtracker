# BugTracker

A full-stack bug tracking application built with **Spring Boot**, **PostgreSQL**, and **vanilla JavaScript**. Includes comprehensive test coverage at every level: unit tests (JUnit + Mockito), integration tests (MockMvc), and end-to-end tests (Selenium + Cucumber BDD).

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-black)

## Features

- **CRUD Operations** — Create, read, update, and delete bug reports
- **Filtering** — Filter bugs by status, priority, or search by title
- **Validation** — Server-side input validation with meaningful error messages
- **Responsive UI** — Clean, modern interface that works on desktop and mobile
- **REST API** — Well-structured RESTful API with proper HTTP status codes

## Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Backend     | Java 17, Spring Boot 3.2, Spring Data JPA |
| Database    | PostgreSQL (H2 for tests)           |
| Frontend    | HTML, CSS, Vanilla JavaScript       |
| Unit Tests  | JUnit 5, Mockito                    |
| Integration | Spring MockMvc                      |
| E2E Tests   | Selenium WebDriver, Cucumber BDD    |
| CI/CD       | GitHub Actions                      |

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/temmunk/bugtracker.git
cd bugtracker
```

### 2. Set up the database

```sql
CREATE DATABASE bugtracker;
```

The default connection settings in `application.properties` expect PostgreSQL running on `localhost:5432` with user `postgres` / password `postgres`. Update as needed.

### 3. Run the application

```bash
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080) in your browser.

## API Endpoints

| Method | Endpoint             | Description            |
|--------|----------------------|------------------------|
| GET    | `/api/bugs`          | List all bugs          |
| GET    | `/api/bugs/{id}`     | Get bug by ID          |
| POST   | `/api/bugs`          | Create a new bug       |
| PUT    | `/api/bugs/{id}`     | Update an existing bug |
| DELETE | `/api/bugs/{id}`     | Delete a bug           |

**Query parameters** for `GET /api/bugs`:
- `status` — Filter by status (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`)
- `priority` — Filter by priority (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
- `search` — Search bugs by title (case-insensitive)

## Running Tests

### Unit + Integration Tests

```bash
mvn test
```

Runs JUnit/Mockito unit tests and Spring MockMvc integration tests using an in-memory H2 database.

### E2E Tests (Selenium + Cucumber)

```bash
mvn verify
```

Runs Cucumber BDD scenarios with Selenium WebDriver. Requires Chrome installed. Tests run headless by default.

## Project Structure

```
src/
├── main/
│   ├── java/com/bugtracker/
│   │   ├── controller/     # REST API controllers
│   │   ├── exception/      # Global error handling
│   │   ├── model/          # JPA entities and enums
│   │   ├── repository/     # Spring Data JPA repositories
│   │   └── service/        # Business logic layer
│   └── resources/
│       ├── static/         # Frontend (HTML, CSS, JS)
│       └── application.properties
└── test/
    ├── java/com/bugtracker/
    │   ├── controller/     # MockMvc integration tests
    │   ├── e2e/            # Cucumber + Selenium E2E tests
    │   └── service/        # Unit tests with Mockito
    └── resources/
        ├── features/       # Cucumber .feature files
        └── application-test.properties
```
