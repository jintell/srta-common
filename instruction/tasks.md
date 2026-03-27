# Task List: `srta-common` Restructuring and Implementation

Based on the `instruction/plan.md`, this list tracks the progress of modularizing the `srta-common` library.

## Phase 1: Gradle & Project Structure (TASK-01-01)
- [x] 1.1 Refactor `settings.gradle` to include all 5 sub-modules: `srta-common-api`, `srta-common-security`, `srta-common-audit`, `srta-common-domain`, and `srta-common-web`.
- [x] 1.2 Update Root `build.gradle` to define common plugins (Java 21, Spring Boot 4.0.5) and shared dependencies (Lombok, Jackson).
- [x] 1.3 Configure sub-module dependencies:
    - [x] `srta-common-web` depends on `srta-common-api`.
    - [x] `srta-common-security` depends on `srta-common-api`.
- [x] 1.4 Create directory structure for each sub-module (`src/main/java`, `src/main/resources`, `src/test/java`).

## Phase 2: Core Components (TASK-01-02 to TASK-01-05)
- [x] 2.1 Implement `srta-common-api`:
    - [x] Create `ApiResponse<T>` envelope.
    - [x] Create `PagedResponse<T>` envelope.
    - [x] Define `ErrorCode` Enum.
    - [x] Create `ValidationError` DTO.
- [x] 2.2 Implement `srta-common-domain`:
    - [x] Create `BaseEntity` with standard audit and versioning fields.
    - [x] Define `TradeStatus` Enum.
    - [x] Define `ProductType` Enum.
- [x] 2.3 Implement `srta-common-web`:
    - [x] Develop `GlobalWebExceptionHandler` for centralized error mapping.
    - [x] Create `CorrelationIdWebFilter` for MDC propagation.
    - [x] Configure `logback-spring.xml` with JSON encoding.
    - [x] Provide base `OpenApiConfig` for Swagger UI.

## Phase 3: Security & Auditing (TASK-01-06 to TASK-01-08)
- [x] 3.1 Implement `srta-common-security`:
    - [x] Configure `ReactiveJwtAuthenticationConverter`.
    - [x] Implement `JwtParser` for claim extraction.
    - [x] Create `SecurityContextHelper` for reactive context interaction.
    - [x] Define `@RequiresPermission` custom annotation.
- [x] 3.2 Implement `srta-common-audit`:
    - [x] Create `AuditEvent` POJO.
    - [x] Set up `AuditPublisher` using RabbitMQ.

## Phase 4: Validation & ArchUnit (TASK-01-09)
- [x] 4.1 Implement ArchUnit Rules:
    - [x] Enforce `@PreAuthorize` on all `@RestController` methods.
    - [x] Ban `float`/`double` in sensitive packages (trade, accounting).
    - [x] Enforce `@JsonIgnore` on sensitive fields like `passwordHash`.
- [x] 4.2 Verify CI pipeline readiness and test coverage (≥ 80%).
