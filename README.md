# `library-api` — RESTful Library Management Service

A production-oriented REST API built with **Java 17**, **Spring Boot**, **PostgreSQL**, **Spring Security (Stateless JWT)**, **MapStruct**, and **OpenAPI / Swagger**. The service manages bibliographic catalogs, library members, and Master-Detail lending workflows with transactional inventory tracking.

## Table of Contents

1. [Features & Functionality](#1-features--functionality)
2. [Architecture Overview](#2-architecture-overview)
3. [Software, Tools & Database Setup](#3-software-tools--database-setup)
4. [Application Properties & Spring Profiles (`dev` / `prod`)](#4-application-properties--spring-profiles-dev--prod)
5. [Database Schema & Seed Data](#5-database-schema--seed-data)
6. [Running the Application](#6-running-the-application)
7. [Usage & Interactive API Docs](#7-usage--interactive-api-docs)
8. [Integrating `library-api` into an Existing Project](#8-integrating-library-api-into-an-existing-project)
9. [Contributing](#9-contributing)
10. [License](#10-license)

## 1. Features & Functionality

* **Catalog & Member Management:** Full CRUD endpoints for book categories, books, and library members (`Member`), tracking both total inventory (`totalCopies`) and real-time availability (`availableCopies`).
* **Master-Detail Lending (`Loan` + `LoanDetail`):** Supports borrowing multiple books and quantities in a single atomic request (`POST /api/loans`), cascading persistence (`CascadeType.ALL`, `orphanRemoval = true`), and loan returns (`PATCH /api/loans/{id}/return`) that automatically restore available copies.
* **Stateless JWT Security:** User registration (`POST /api/auth/register`) and login (`POST /api/auth/login`) with BCrypt password hashing, HMAC token signing and validation (`JwtService`), per-request authentication (`JwtAuthenticationFilter`), and role-based access control (`ROLE_USER`, `ROLE_ADMIN`).
* **Clean DTO Contracts & Centralized Error Handling:** Decouples JPA persistence entities from external JSON payloads using MapStruct DTO mappers, validates inputs via Bean Validation (`@Valid`), and standardizes error responses (`400`, `401`, `403`, `404`) through `@RestControllerAdvice` and custom security handlers.
* **Interactive OpenAPI / Swagger UI:** Fully annotated endpoints with `Bearer` JWT security schemes and payload examples for rapid testing and client integration.

## 2. Architecture Overview

The project follows a layered backend architecture:

```text
src/main/java/com/gmao/itm/library_api/
├── config/        # OpenAPI/Swagger and embedded Tomcat configuration
├── controller/    # REST controllers, HTTP status mapping, and OpenAPI annotations
├── dto/           # Request and Response Data Transfer Objects
├── entity/        # JPA entities (Category, Book, Member, Loan, LoanDetail, User)
├── exception/     # Custom domain exceptions and global @RestControllerAdvice
├── mapper/        # MapStruct interfaces for Entity <-> DTO conversion
├── repository/    # Spring Data JPA repositories
├── security/      # SecurityConfig, JwtService, JwtAuthenticationFilter, handlers
└── service/       # @Transactional business logic and inventory rules
```

## 3. Software, Tools & Database Setup

### Technology Stack

| Category | Tool / Technology | Role in Project |
| :--- | :--- | :--- |
| **Language & Runtime** | Java 17 (OpenJDK / Temurin) | Core runtime environment |
| **Framework & Build** | Spring Boot, Gradle Wrapper | Web layer, JPA, Security, dependency injection, and build automation |
| **Database** | PostgreSQL | Relational persistence for users, categories, books, members, and loans |
| **Object Mapping** | MapStruct | Compile-time mapping between JPA entities and DTOs |
| **Security** | Spring Security + JJWT | Stateless Bearer JWT authentication and role authorization |
| **API Documentation** | Springdoc OpenAPI / Swagger UI | Interactive API exploration and endpoint testing |

### Installation & Local Tooling

1. **Java 17:** Install JDK 17 and verify with `java -version`.
2. **Gradle:** Use the included Gradle Wrapper (`./gradlew` on Linux/macOS or `gradlew.bat` on Windows) to guarantee consistent builds.
3. **PostgreSQL:** Install and start the PostgreSQL service, then create the target database:
   ```sql
   CREATE DATABASE library_db;
   ```
4. **IDE & Git:** Open the repository in **IntelliJ IDEA** (or your preferred Java IDE), enable annotation processing (required for MapStruct/Lombok), and configure JDK 17.

## 4. Application Properties & Spring Profiles (`dev` / `prod`)

Spring Boot separates shared baseline settings from environment-specific configuration using profiles (`dev` for local development and `prod` for production deployments). Versioned `.example` templates are included in `src/main/resources/` so developers can bootstrap local configuration without committing secrets.

### Configuration Layout (`src/main/resources/`)

```text
src/main/resources/
├── application.properties                  # Shared base properties & active profile selector
├── application-dev.properties.example      # Versioned template for local development
├── application-prod.properties.example     # Versioned template for production deployments
├── application-dev.properties              # Active local dev config (git-ignored)
└── application-prod.properties             # Active prod config (git-ignored)
```

### Setting Up Profile Files from `.example` Templates

1. Copy the desired template inside `src/main/resources/`:
   ```bash
   cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
   ```
   *(For production deployments, copy `application-prod.properties.example` to `application-prod.properties`.)*
2. Confirm that `application-dev.properties` and `application-prod.properties` are listed in `.gitignore`.
3. Provide the required environment variables (`DB_PASSWORD`, `JWT_SECRET`, etc.) in your shell or IDE Run Configuration.

### Base `application.properties`

`application.properties` defines common settings, default profile activation (`spring.profiles.active`), and placeholders resolved from OS environment variables:

```properties
spring.application.name=library-api
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}

# PostgreSQL Datasource (resolved via environment variables)
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/library_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate Shared Settings
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Security Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# Swagger / OpenAPI Paths
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### `dev` vs. `prod` Profile Behavior

| Setting | `dev` Profile (`application-dev.properties`) | `prod` Profile (`application-prod.properties`) |
| :--- | :--- | :--- |
| **Template Reference** | `application-dev.properties.example` | `application-prod.properties.example` |
| **Database Schema (`ddl-auto`)** | `update` (auto-syncs schema changes during local development) | `validate` (enforces schema integrity without altering production tables) |
| **SQL & Debug Logging** | `spring.jpa.show-sql=true` and `DEBUG` logs for troubleshooting | `spring.jpa.show-sql=false` and `WARN`/`INFO` logs to protect performance and data |
| **Swagger UI** | Enabled (`/swagger-ui.html`) for interactive testing | Disabled by default (`${SWAGGER_ENABLED:false}`) |
| **Error Details** | Detailed messages for local debugging | Stack traces and internal messages hidden (`never`) |
| **Secrets Management** | Injected via IDE Run Configuration or local shell variables | Injected strictly via server/container environment variables |

### Required Environment Variables

Never commit plaintext database passwords or signing keys to Git. Configure these variables before running the application:

| Variable | Description | Example |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring Boot profile (`dev` or `prod`) | `dev` |
| `DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/library_db` |
| `DB_USERNAME` | PostgreSQL database user | `postgres` |
| `DB_PASSWORD` | PostgreSQL database password | `your_secure_db_password` |
| `JWT_SECRET` | High-entropy secret key ($\ge 256$ bits / 32+ chars) for HMAC JWT signing | `your_256_bit_secret_key` |
| `JWT_EXPIRATION` | JWT token validity duration in milliseconds (default: 24h in dev, 1h in prod) | `86400000` |
| `SWAGGER_ENABLED` | Toggle OpenAPI/Swagger endpoints in `prod` (`true` / `false`) | `false` |

### Switching Profiles at Runtime

* **Run in `dev` mode (default):**
  ```bash
  ./gradlew bootRun --args='--spring.profiles.active=dev'
  ```
* **Run in `prod` mode:**
  ```bash
  SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
  ```

## 5. Database Schema & Seed Data

The relational model enforces referential integrity, copy-count constraints, and Master-Detail cascade deletion between `loans` and `loan_details`. A complete SQL script is provided in [`schema.sql`](schema.sql).

### Entity-Relationship Summary

* `categories (1) ──< (N) books`: Each book belongs to a category (`fk_books_category`).
* `members (1) ──< (N) loans`: Each loan header is associated with a library member (`fk_loans_member`).
* `loans (1) ──< (N) loan_details`: Master-Detail relationship with `ON DELETE CASCADE` (`fk_loan_details_loan`).
* `books (1) ──< (N) loan_details`: Links each loan line item to a specific book (`fk_loan_details_book`).
* `users`: Stores application credentials (`username`, BCrypt `password`, and `role`).

### Executing `schema.sql`

Run the script against `library_db` using `psql` (or execute it in pgAdmin / DBeaver / IntelliJ Database Console):

```bash
psql -U postgres -d library_db -f schema.sql
```

This initializes all 6 tables (`users`, `categories`, `books`, `members`, `loans`, `loan_details`), performance indexes, check constraints (`available_copies <= total_copies`), and initial seed records so you can immediately query endpoints and test loan returns.

## 6. Running the Application

1. **Verify PostgreSQL is running** and `schema.sql` (or Hibernate DDL in `dev`) has initialized `library_db`.
2. **Prepare local profile & export environment variables** in your terminal or IntelliJ IDEA (`Run > Edit Configurations`):
   ```bash
   cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
   export SPRING_PROFILES_ACTIVE=dev
   export DB_URL=jdbc:postgresql://localhost:5432/library_db
   export DB_USERNAME=postgres
   export DB_PASSWORD=your_secure_db_password
   export JWT_SECRET=your_256_bit_secret_key
   ```
3. **Start the server** using the Gradle Wrapper:
   ```bash
   ./gradlew bootRun
   ```
4. **Confirm startup:** Wait for `Started LibraryApiApplication` on port `8080`. Embedded Tomcat is preconfigured via `TomcatConfig` (`maxLocaleCount = 50`) to accept multi-locale browser `Accept-Language` headers without errors.

## 7. Usage & Interactive API Docs

Once the application is running, open Swagger UI in your browser:

* **Swagger UI:** `http://localhost:8080/swagger-ui.html`
* **OpenAPI JSON Spec:** `http://localhost:8080/v3/api-docs`

### Standard Workflow

1. **Register or Log In:** Send credentials to `POST /api/auth/register` or `POST /api/auth/login` and copy the returned `token` string.
2. **Authorize Swagger UI:** Click the **Authorize** button at the top right, paste the JWT token into the `Value` field (without the `Bearer ` prefix), and click **Authorize**.
3. **Manage Catalog & Members:** Create or query categories (`/api/categories`), books (`/api/books`), and members (`/api/members`).
4. **Create a Master-Detail Loan (`POST /api/loans`):**
   ```json
   {
     "memberId": 1,
     "loanDate": "2026-11-23",
     "dueDate": "2026-11-30",
     "details": [
       { "bookId": 1, "quantity": 1 },
       { "bookId": 2, "quantity": 1 }
     ]
   }
   ```
   *Verifies active member status and stock availability, persists the `Loan` header and `LoanDetail` rows in cascade, and decrements `availableCopies` on each book.*
5. **Return a Loan (`PATCH /api/loans/{id}/return`):** Marks the loan as `RETURNED`, sets `returnDate`, and restores `availableCopies` across all associated books.

## 8. Integrating `library-api` into an Existing Project

To consume `library-api` from a web frontend, mobile client (e.g., Flutter), or another backend service:

1. **Configure Base URL & Timeouts:** Point your HTTP client to `http://localhost:8080` (or your deployed API base URL) with explicit connection and read timeouts.
2. **Authenticate & Store Token:** Call `POST /api/auth/login` with valid credentials and store the returned JWT securely (e.g., encrypted storage on mobile or memory/HTTP client interceptor on web).
3. **Attach `Authorization` Header:** Add an HTTP interceptor that injects the header on every protected request:
   ```http
   Authorization: Bearer <your_jwt_token>
   ```
4. **Handle Standard HTTP Status Codes:**
   * `200 OK` / `201 Created`: Parse the JSON response DTO.
   * `400 Bad Request`: Validation error (`@Valid`) or business rule violation (e.g., insufficient stock or returning an already-returned loan); display the structured error message to the user.
   * `401 Unauthorized`: Missing, malformed, or expired JWT; clear local credentials and prompt re-authentication.
   * `403 Forbidden`: Authenticated user lacks the required role (such as `ROLE_ADMIN` for `DELETE` operations).
   * `404 Not Found`: Requested resource ID does not exist.

## 9. Contributing

1. **Sync `main`:**
   ```bash
   git checkout main
   git pull origin main
   ```
2. **Create a feature or fix branch:**
   ```bash
   git checkout -b feat/your-feature-name
   ```
3. **Follow project conventions:**
   * Keep DTOs decoupled from JPA entities using MapStruct.
   * Wrap multi-entity state changes in `@Transactional` service methods.
   * Never commit `.env` files, active `application-dev.properties` / `application-prod.properties` files, or plaintext secrets.
   * Write clear commit messages in English (e.g., `feat: ...`, `fix: ...`, `docs: ...`).
4. **Verify before opening a Pull Request:** Ensure `./gradlew test` and `./gradlew build` pass cleanly and update Swagger annotations if endpoint contracts change.

## 10. License

This project is licensed under the **MIT License** — see the [`LICENSE`](LICENSE.md) file in the root directory for details.