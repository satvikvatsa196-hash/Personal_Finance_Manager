# Personal Finance Manager API

Spring Boot 3 / Java 17 implementation of the Syfe backend intern assignment. The API uses session-cookie authentication and stores data in memory, which keeps the public test script deterministic for a fresh deployment.

## Features

- User registration, login, logout, and session-based authentication
- Default categories plus per-user custom categories
- Transaction CRUD with date, category, and type filtering
- Savings goals with live progress calculation
- Monthly and yearly reports grouped by category
- Global validation and JSON error responses

## Run Locally

```bash
mvn spring-boot:run
```

The API starts at `http://localhost:8080/api`.

Run tests and generate the JaCoCo report:

```bash
mvn test
```

Coverage output is written to `target/site/jacoco/index.html`.

## Deployment on Render

Create a new Render Web Service from the GitHub repository. This repo also includes a `Dockerfile` and `render.yaml`, so Render can build it as a Docker web service.

- Runtime: Docker
- Dockerfile path: `./Dockerfile`

Render provides the `PORT` environment variable automatically; `application.properties` maps it to `server.port`.

## API Summary

Public endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`

Authenticated endpoints:

- `POST /api/auth/logout`
- `GET /api/categories`
- `POST /api/categories`
- `DELETE /api/categories/{name}`
- `POST /api/transactions`
- `GET /api/transactions`
- `PUT /api/transactions/{id}`
- `DELETE /api/transactions/{id}`
- `POST /api/goals`
- `GET /api/goals`
- `GET /api/goals/{id}`
- `PUT /api/goals/{id}`
- `DELETE /api/goals/{id}`
- `GET /api/reports/monthly/{year}/{month}`
- `GET /api/reports/yearly/{year}`

Use the supplied script after deployment:

```bash
bash financial_manager_tests.sh https://your-render-service.onrender.com/api
```
