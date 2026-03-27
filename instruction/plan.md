# Improvement Plan: `srta-common` Shared Library

Based on the requirements in `instruction/requirement.md`, this plan outlines the restructuring and implementation of the `srta-common` shared library into a modular architecture.

## 1. Modular Architecture Restructuring
The current single-module `srta-common` will be converted into a multi-module Gradle project to separate concerns and allow granular dependency management.

### Sub-module: `srta-common-api`
- **Purpose**: Core API response models and error definitions.
- **Key Components**:
    - `ApiResponse<T>`: Standard envelope for all API responses (SUCCESS, ERROR, VALIDATION_ERROR).
    - `PagedResponse<T>`: Standard envelope for paginated data.
    - `ErrorCode` Enum: Centralized list of application error codes.
    - `ValidationError` DTO: Details for field-level validation errors.

### Sub-module: `srta-common-security`
- **Purpose**: Security utilities for JWT and permission handling.
- **Key Components**:
    - `JwtParser`: Reactive component to extract claims from JWT tokens.
    - `SecurityContextHelper`: Utility to interact with `ReactiveSecurityContextHolder`.
    - `@RequiresPermission`: Custom annotation for fine-grained authorization (to be used alongside `@PreAuthorize`).

### Sub-module: `srta-common-audit`
- **Purpose**: Auditing system for tracking critical events.
- **Key Components**:
    - `AuditEvent`: POJO representing an auditable action.
    - `AuditPublisher`: Reactive RabbitMQ sender for audit logs.

### Sub-module: `srta-common-domain`
- **Purpose**: Base classes and shared domain enums.
- **Key Components**:
    - `BaseEntity`: Record/Class with `id`, `createdAt`, `updatedAt`, and `version` (optimistic locking).
    - `TradeStatus` Enum: Standard lifecycle statuses for trades.
    - `ProductType` Enum: Supported financial products (FX_SPOT, FX_FORWARD, FX_SWAP).

### Sub-module: `srta-common-web`
- **Purpose**: Shared web infrastructure for Reactive applications.
- **Key Components**:
    - `GlobalWebExceptionHandler`: Centralized `@ControllerAdvice` for mapping exceptions to `ApiResponse`.
    - `CorrelationIdWebFilter`: Generates and propagates `X-Correlation-ID` via MDC and headers.

---

## 2. Implementation Roadmap

### Phase 1: Gradle & Project Structure (TASK-01-01)
1.  **Refactor `settings.gradle`**: Include all 5 sub-modules.
2.  **Update Root `build.gradle`**: Define common plugins (Java 21, Spring Boot 4.0.5, Dependency Management) and shared dependencies (Lombok, Jackson).
3.  **Define Sub-module Dependencies**:
    - `srta-common-web` depends on `srta-common-api`.
    - `srta-common-security` depends on `srta-common-api`.

### Phase 2: Core Components (TASK-01-02 to TASK-01-05)
1.  **Implement `srta-common-api`**: Create the response envelopes and base error codes.
2.  **Implement `srta-common-domain`**: Create `BaseEntity` and core enums using Spring Data R2DBC annotations.
3.  **Implement `srta-common-web`**:
    - Develop the `GlobalWebExceptionHandler` to handle `WebExchangeBindException` (Validation) and `ResponseStatusException`.
    - Create `CorrelationIdWebFilter` using `ContextRegistry` for MDC propagation in Reactor.

### Phase 3: Security & Auditing (TASK-01-06 to TASK-01-08)
1.  **Implement `srta-common-security`**: Configure `ReactiveJwtAuthenticationConverter` and utility classes for JWT parsing.
2.  **Implement `srta-common-audit`**: Set up `AuditPublisher` using `spring-cloud-stream-binder-rabbit` or direct `Sender` from Reactor RabbitMQ.

### Phase 4: Validation & ArchUnit (TASK-01-09)
1.  **Implement ArchUnit Rules**:
    - Enforce `@PreAuthorize` on all `@RestController` methods.
    - Ban `float`/`double` in sensitive packages (trade, accounting).
    - Enforce `@JsonIgnore` on sensitive fields like `passwordHash`.

---

## 3. Infrastructure & Logging
- **Logging**: Configure `logback-spring.xml` in `srta-common-web` using `LogstashEncoder` to include `correlationId`, `traceId`, and `userId` in JSON format.
- **OpenAPI**: Provide a base `OpenApiConfig` in `srta-common-web` that pre-configures JWT Bearer Auth for Swagger UI.

## 4. Testing Strategy
1.  **Unit Tests**: Use `StepVerifier` for all reactive logic in `JwtParser` and `AuditPublisher`.
2.  **Integration Tests**: Use `@WebFluxTest` to verify `GlobalWebExceptionHandler` mapping.
3.  **ArchUnit Tests**: Integrated into the `srta-common` test suite to fail the build on rule violations.

---

## 5. Success Metrics
- CI pipeline green with ≥ 80% coverage on `srta-common` logic.
- Zero ArchUnit violations.
- Successful publication to Maven registry (local/internal).
