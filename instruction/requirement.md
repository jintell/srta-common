## 1. Platform Foundation & Shared Library

**Service Description:** Establishes the multi-module project skeleton, `srta-common` shared library, CI/CD pipelines, database schema baseline, and API Gateway. This is the prerequisite layer — no feature work begins until all foundation stories are complete.

**Goals:**
- All 10 services boot in Docker Compose and are independently deployable
- CI/CD pipeline compiles, tests, scans, and deploys without manual intervention
- Flyway migrations establish the complete schema in both PostgreSQL and Oracle
- API Gateway routes, authenticates, and rate-limits all client traffic

**Completion Criteria:**
- All 10 services start clean in Docker Compose (`docker-compose up`) with zero errors
- CI pipeline runs green: build → unit tests → integration tests → coverage ≥ 80% service layer → OWASP Dependency-Check (CVSS ≥ 7.0 fails build) → Docker build/push
- Flyway migrations applied cleanly to PostgreSQL 15 and Oracle 19c
- Swagger UI accessible at `http://localhost:{port}/swagger-ui.html` for every service
- API Gateway routes `POST /api/v1/auth/**` to auth-service, `GET /api/v1/trades/**` to trade-service, etc.
- Local developer onboarding takes < 30 minutes following the README

---

### Design Project Scaffolding & Shared Library [TASK-01-01 to TASK-01-10]

**Requirements:**
- Mono-repo or multi-repo Git structure with all 10 microservice modules: `auth-service` (8081), `user-service` (8082), `trade-service` (8083), `quote-service` (8084), `config-service` (8085), `report-service` (8086), `document-service` (8087), `notification-service` (8088), `integration-service` (8089), `api-gateway` (8080)
- Each service is a Spring Boot 3.x WebFlux application with its own `application.yml`
- `srta-common` published to internal Maven registry; consumed by all services via `implementation project(':srta-common')`
- `docker-compose.yml` runs all services + PostgreSQL 15 + Redis 7 + RabbitMQ 3.12 locally
- Swagger UI accessible at `/swagger-ui.html`; OpenAPI spec at `/v3/api-docs` per service
- Global `WebExceptionHandler` returns `ApiResponse` envelope for all errors — never raw exceptions or stack traces
- Environment-specific property files: `application-dev.yml`, `application-sit.yml`, `application-uat.yml`, `application-prod.yml`
- Branch strategy: `main` (production-ready), `develop` (integration), `feature/*`, `release/*`, `hotfix/*`

**Provide:**

*Deliverables:*
- `srta-common/srta-common-api/`: `ApiResponse<T>`, `PagedResponse<T>`, `ErrorCode` enum, `ValidationError` DTO
- `srta-common/srta-common-security/`: `JwtParser`, `SecurityContextHelper`, `@RequiresPermission` annotation
- `srta-common/srta-common-audit/`: `AuditEvent`, `AuditPublisher` (RabbitMQ reactive sender)
- `srta-common/srta-common-domain/`: `BaseEntity` (id, createdAt, updatedAt, version), `TradeStatus` enum (`NONE`, `TRADE_ENTERED`, `PENDING`, `APPROVED`, `SETTLED`, `CANCELLED`, `CANCEL_REQUESTED`, `SETTLEMENT_FAILED`), `ProductType` enum (`FX_SPOT`, `FX_FORWARD`, `FX_SWAP`)
- `srta-common/srta-common-web/`: Global reactive `WebExceptionHandler`, `CorrelationIdWebFilter` (generates `X-Correlation-ID` if absent, injects into MDC + response header)
- `docker-compose.yml` with health-checks for PostgreSQL, Redis, RabbitMQ; `depends_on` for service startup ordering
- Logback configuration: `logback-spring.xml` using `LogstashEncoder`; MDC keys: `correlationId`, `traceId`, `spanId`, `userId`, `dealId`, `service`, `env`
- `springdoc-openapi-starter-webflux-ui` dependency in each service; OpenAPI bean configured with JWT bearer scheme
- Environment property files with placeholder values; secrets via `${ENV_VAR}` references only

*Validation Rules:*
- `ApiResponse.status` must be one of: `SUCCESS`, `ERROR`, `VALIDATION_ERROR`
- `ErrorCode` enum must contain all standard codes (see Section 12)
- Every `@RestController` class must be annotated `@PreAuthorize` on each method — enforced by ArchUnit test
- `float` and `double` types forbidden in packages `*.trade.*`, `*.accounting.*`, `*.quote.*` — ArchUnit rule fails CI build if violated
- `@JsonIgnore` required on `passwordHash`, `refreshTokenHash` fields — ArchUnit rule

*Error Handling:*
- Unhandled `Exception` → HTTP 500, code `INTERNAL_ERROR`, message `"An unexpected error occurred"`; full stack trace logged server-side only
- `MethodArgumentNotValidException` / `WebExchangeBindException` → HTTP 400, status `VALIDATION_ERROR`, field-level errors array
- `AccessDeniedException` → HTTP 403, code `ACCESS_DENIED`
- `ResponseStatusException` → mapped to its status code

*Test Cases:*
- TC-FOUND-01: Start all 10 services via `docker-compose up`; all report `UP` on `/actuator/health/readiness` within 60 seconds
- TC-FOUND-02: POST `/api/v1/trades` with invalid JWT → API Gateway returns HTTP 401, `AUTHENTICATION_FAILED`, no stack trace in response body
- TC-FOUND-03: Request without `X-Correlation-ID` header → Gateway generates one; all downstream service logs include same correlation ID
- TC-FOUND-04: Request with invalid JSON body → `ValidationError` response with field-level errors; HTTP 400
- TC-FOUND-05: ArchUnit test run → build fails if any `@RestController` method lacks `@PreAuthorize`

*Edge Cases:*
- Service starts with RabbitMQ unavailable: services with optional MQ consumers must boot with a health warning (`DEGRADED`), not crash
- Flyway migration fails on startup: application must not start; clear error in logs with migration version and script name
- Redis unavailable at startup: API Gateway rate limiting bypasses with warning log; JWT blacklist check skips (brief security risk window logged)
- `srta-common` version mismatch (older consumer service): semantic versioning enforced; breaking changes require major version bump

---