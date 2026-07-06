# Cloud File Upload Service

A Spring Boot backend for uploading, listing, downloading, and deleting files against an
S3-compatible object store (MinIO), with per-user access control and file metadata persisted
in an embedded H2 database. Interactive API documentation is generated automatically with
springdoc-openapi.

## Features

- Upload, list, download, and delete files, scoped to the authenticated user
- HTTP Basic authentication with BCrypt-hashed passwords
- S3-compatible object storage via MinIO (swap the endpoint for real AWS S3 with no code changes)
- Embedded H2 database for file/user metadata — no external database to install
- Interactive Swagger UI for exploring and testing every endpoint
- Global error handling with consistent JSON error responses
- Unit, web-layer (MockMvc), and repository (`@DataJpaTest`) test coverage

## Tech stack

Java 17, Spring Boot 3, Spring Security, Spring Data JPA, H2, MinIO Java SDK, springdoc-openapi.

## Running locally

You need Docker (for MinIO) and a JDK 17+. A database is **not** required — H2 is embedded.

1. Start MinIO:

   ```bash
   docker compose up -d
   ```

   This starts MinIO on `http://localhost:9000` (console at `http://localhost:9001`,
   login `minioadmin` / `minioadmin123`).

2. Run the app:

   ```bash
   ./mvnw spring-boot:run
   ```

   The app boots on `http://localhost:8080`. On first startup it creates the configured
   MinIO bucket automatically. If MinIO isn't reachable yet, the app still starts — file
   upload/download will simply fail until MinIO is available.

3. Open the API docs: **http://localhost:8080/swagger-ui.html**

To point at a different MinIO endpoint/bucket (or real AWS S3), set the environment variables
in `.env.example` before starting the app — no code changes needed.

## Database

Metadata is stored in an embedded H2 database, written to `./data/uploaddb` by default so data
survives restarts. To inspect it directly, use the H2 console at `http://localhost:8080/h2-console`
with:

- JDBC URL: `jdbc:h2:file:./data/uploaddb;AUTO_SERVER=TRUE`
- User: `sa`, no password

## API overview

All endpoints require HTTP Basic auth except registration.

| Method | Path                           | Description                            |
|--------|--------------------------------|-----------------------------------------|
| POST   | `/api/v1/users/register`       | Register a new user (public)            |
| POST   | `/api/v1/files`                 | Upload a file (`multipart/form-data`)   |
| GET    | `/api/v1/files`                  | List the authenticated user's files     |
| GET    | `/api/v1/files/{id}`             | Get metadata for one file               |
| GET    | `/api/v1/files/{id}/download`    | Download a file's content               |
| DELETE | `/api/v1/files/{id}`             | Delete a file                           |

### Example: register + upload

```bash
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"supersecret"}'

curl -X POST http://localhost:8080/api/v1/files \
  -u user@example.com:supersecret \
  -F "file=@/path/to/document.pdf"
```

Full request/response schemas are available in Swagger UI, or as raw OpenAPI JSON at
`/v3/api-docs`.

## Running tests

```bash
./mvnw test
```

Tests run against an in-memory H2 database and never touch a real MinIO instance
(the MinIO bucket bootstrap step is disabled under the `test` profile).

## Project structure

```
com.fileuploader.upload
├── config          # Security, MinIO client, OpenAPI configuration
├── controllers     # REST controllers
├── dto             # Request/response records
├── entities        # JPA entities
├── exceptions      # Custom exceptions + global @RestControllerAdvice
├── init            # Startup bucket bootstrap
├── repositories    # Spring Data JPA repositories
├── security        # UserDetailsService implementation
└── services        # Business logic (user + file services, blob storage abstraction)
```

## Deploying

The included `Dockerfile` builds a self-contained image (multi-stage build, non-root user).
MinIO/S3 credentials and the bucket name are the only required runtime configuration
(see `.env.example`); the database needs no setup since it's embedded.
