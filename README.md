# Library Management REST API (`library-api`)

A secure, documented REST API for managing a library system (`Category`, `Book`, `Member`, `Loan`, `LoanDetail`, and `User`), built with **Java 17**, **Spring Boot 4.x**, **Gradle**, **PostgreSQL**, **MapStruct**, **Spring Security (JWT)**, and **Springdoc OpenAPI (Swagger UI)**.

## Architecture & Domain Model

- **Catalog Entities**: `Category` (`1 -> N` `Book`), `Book` (`N -> 1` `Category`).
- **Master-Detail Aggregate**: `Loan` (`N -> 1` `Member`, `1 -> N` `LoanDetail` with `CascadeType.ALL` and `orphanRemoval = true`).
- **Security**: `User` with `Role` (`ROLE_ADMIN`, `ROLE_USER`) and stateless JWT Bearer authentication.
- **Layers**:
  - `controller`: REST endpoints under `/api/auth`, `/api/categories`, `/api/books`, `/api/members`, and `/api/loans`.
  - `service`: `@Transactional` business logic, inventory copy adjustments, fee calculations, and cascade persistence.
  - `mapper`: MapStruct interfaces mapping between JPA entities and request/response DTOs.
  - `exception`: Domain exceptions and `@RestControllerAdvice` returning consistent HTTP `400`, `401`, `403`, `404`, and `409` responses.
  - `security` & `config`: `JwtService`, `JwtAuthenticationFilter`, `CustomUserDetailsService`, `SecurityConfig`, and `OpenApiConfig`.

## Configuration & Environment Variables

Configuration is centralized in `src/main/resources/application.properties` with local `dev` and `prod` profile files (`application-dev.properties` and `application-prod.properties` ignored in Git). Provide credentials and secrets via IDE/system environment variables:

- `SPRING_PROFILES_ACTIVE` (`dev` by default, or `prod`)
- `DB_URL` (e.g., `jdbc:postgresql://localhost:5432/library_db`)
- `DB_USERNAME` (e.g., `postgres`)
- `DB_PASSWORD` (PostgreSQL password)
- `JWT_SECRET` (at least 32 characters / 256-bit secret)
- `JWT_EXPIRATION_MS` (optional, default `86400000`)

## Running the Application

```bash
./gradlew bootRun