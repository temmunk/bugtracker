# BugTracker

A full-stack bug tracking application built with **Spring Boot**, **PostgreSQL**, and **vanilla JavaScript**. Features a dashboard with analytics, comments, activity audit log, CSV export, pagination, and comprehensive test coverage at every level.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-black)

## Features

- **CRUD Operations** — Create, read, update, and delete bug reports
- **Dashboard** — Real-time stats with bug counts by status, priority, and top assignees
- **Comments** — Add threaded comments to any bug for team collaboration
- **Activity Log** — Automatic audit trail tracking every create, update, and delete action
- **Filtering & Search** — Filter by status/priority or search by title
- **Pagination & Sorting** — Server-side pagination with sortable columns
- **CSV Export** — Download all bug data as a CSV file
- **Validation** — Server-side input validation with meaningful error messages
- **Responsive UI** — Clean, modern interface that works on desktop and mobile
- **REST API** — Well-structured RESTful API with proper HTTP status codes and error handling

## Tech Stack

| Layer       | Technology                                |
|-------------|-------------------------------------------|
| Backend     | Java 17, Spring Boot 3.2, Spring Data JPA |
| Database    | PostgreSQL (H2 for tests)                 |
| Frontend    | HTML, CSS, Vanilla JavaScript             |
| Unit Tests  | JUnit 5, Mockito                          |
| Integration | Spring MockMvc                            |
| E2E Tests   | Selenium WebDriver, Cucumber BDD          |
| CI/CD       | GitHub Actions                            |

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## API Endpoints

### Bugs

| Method | Endpoint             | Description            |
|--------|----------------------|------------------------|
| GET    | `/api/bugs`          | List bugs (paginated)  |
| GET    | `/api/bugs/{id}`     | Get bug by ID          |
| POST   | `/api/bugs`          | Create a new bug       |
| PUT    | `/api/bugs/{id}`     | Update an existing bug |
| DELETE | `/api/bugs/{id}`     | Delete a bug           |
| GET    | `/api/bugs/stats`    | Dashboard statistics   |
| GET    | `/api/bugs/export`   | Export all bugs to CSV |

**Query parameters** for `GET /api/bugs`:
- `status` — Filter by status (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`)
- `priority` — Filter by priority (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
- `search` — Search bugs by title (case-insensitive)
- `page` — Page number (default: 0)
- `size` — Page size (default: 10)
- `sortBy` — Sort field (default: `createdAt`)
- `sortDir` — Sort direction: `asc` or `desc` (default: `desc`)

### Comments

| Method | Endpoint                          | Description             |
|--------|-----------------------------------|-------------------------|
| GET    | `/api/bugs/{id}/comments`         | List comments for a bug |
| POST   | `/api/bugs/{id}/comments`         | Add a comment           |
| DELETE | `/api/bugs/{id}/comments/{cid}`   | Delete a comment        |

### Activity Log

| Method | Endpoint                  | Description                  |
|--------|---------------------------|------------------------------|
| GET    | `/api/activity`           | Recent activity (last 20)    |
| GET    | `/api/activity/bug/{id}`  | Activity history for a bug   |

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
│   │   ├── controller/     # REST API controllers (Bug, Comment, ActivityLog)
│   │   ├── dto/            # Data transfer objects (DashboardStats)
│   │   ├── exception/      # Global error handling
│   │   ├── model/          # JPA entities (Bug, Comment, ActivityLog)
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
