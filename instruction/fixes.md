# srta-common Code Review — Architect Findings

> Reviewed: 2026-03-27 | Reviewer: Senior Backend Architect

> Scope: 
> - Security
> - Logic
> - Validation
> - API Design
> - Test Coverage
> - Production Readiness

---

## SEVERITY LEGEND
- **[CRITICAL]** — Must fix before any production deployment
- **[HIGH]** — Fix before first consumer service ships
- **[MEDIUM]** — Fix in next sprint
- **[LOW]** — Improvement / tech debt

---

## 1. SECURITY ISSUES

### [CRITICAL] S-1 — NOT_FOUND silently maps to INTERNAL_ERROR
**File:** `srta-common-web/GlobalWebExceptionHandler.java:59`

```java
case NOT_FOUND -> ErrorCode.INTERNAL_ERROR; // wrong mapping
```

Returning `500 INTERNAL_ERROR` for a `404 NOT_FOUND` is incorrect HTTP semantics and can mask real errors in monitoring. More importantly, logging `log.error` for routine 404s will flood error dashboards.

**Fix:**
```java
case NOT_FOUND -> ErrorCode.TRADE_NOT_FOUND; // or a generic RESOURCE_NOT_FOUND
```
Add `RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource does not exist")` to `ErrorCode` as a generic fallback for 404s, and use `log.warn` not `log.error` for client errors (4xx).

---

### [CRITICAL] S-2 — CONFLICT maps to USERNAME_EXISTS — incorrect and leaks internal logic
**File:** `srta-common-web/GlobalWebExceptionHandler.java:62`

```java
case CONFLICT -> ErrorCode.USERNAME_EXISTS; // too specific, semantically wrong
```

A `409 CONFLICT` can arise from many reasons (email conflict, optimistic lock collision, duplicate entity). Hardcoding `USERNAME_EXISTS` means a trade duplicate conflict will return a confusing "Duplicate username" message to consumers.

**Fix:** Add `CONFLICT("CONFLICT", "Resource conflict or duplicate")` to `ErrorCode` as the default `409` mapping.

---

### [CRITICAL] S-3 — SERVICE_UNAVAILABLE always maps to CBS_UNAVAILABLE
**File:** `srta-common-web/GlobalWebExceptionHandler.java:65`

```java
case SERVICE_UNAVAILABLE -> ErrorCode.CBS_UNAVAILABLE; // TMS and others are silently CBS
```

TMS, SMTP, and other downstream failures will all return `CBS_UNAVAILABLE` to the client, which is misleading.

**Fix:** Add `DOWNSTREAM_UNAVAILABLE("DOWNSTREAM_UNAVAILABLE", "Downstream service unavailable")` as the default `503` mapping.

---

### [HIGH] S-4 — MDC.put() inside doOnEach is not thread-safe in reactive streams
**File:** `srta-common-web/CorrelationIdWebFilter.java:39-43`

```java
.doOnEach(signal -> {
    MDC.put(CORRELATION_ID_KEY, finalCorrelationId); // NOT safe — wrong thread
});
```

`MDC` is `ThreadLocal`-based. In a reactive pipeline threads switch freely; the `MDC.put()` may execute on a scheduler thread that doesn't handle the actual log statement, so correlation IDs are silently lost. The current code also **never calls `MDC.remove()`**, causing MDC leakage in thread pools.

**Fix:** Replace with Micrometer Tracing or reactor-context-propagation (available since Reactor 3.5):
```java
return chain.filter(exchange)
    .contextWrite(Context.of(CORRELATION_ID_KEY, finalCorrelationId));
// Register a global Hook in a @Configuration class:
// Hooks.enableAutomaticContextPropagation();
```
This propagates the Reactor `Context` automatically to MDC without manual threading.

---

### [HIGH] S-5 — No SecurityFilterChain bean defined
**File:** `srta-common-security` module (missing file)

The module pulls in `spring-boot-starter-oauth2-resource-server` but defines no `SecurityFilterChain`. Any consumer that imports this module without defining their own will get Spring Security's default form-login, which **exposes a login page** on a resource server and disables stateless JWT validation.

**Fix:** Add a `ReactiveSecurityAutoConfiguration.java` with a `@ConditionalOnMissingBean` `SecurityWebFilterChain` that sets stateless JWT validation as the sensible default:
```java
@Bean
@ConditionalOnMissingBean(SecurityWebFilterChain.class)
public SecurityWebFilterChain defaultSecurityFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .authorizeExchange(ex -> ex.anyExchange().authenticated())
        .build();
}
```

---

### [HIGH] S-6 — ReactiveJwtAuthenticationConverter performs no authority mapping
**File:** `srta-common-security/ReactiveJwtAuthenticationConverter.java`

```java
public ReactiveJwtAuthenticationConverter() {
    super(new JwtAuthenticationConverter()); // default — maps scope_ claims only
}
```

The default `JwtAuthenticationConverter` only maps `scope` claims with the `SCOPE_` prefix. Your `JwtParser` reads `permissions` or `roles` claims — but this converter does not, so Spring's `@PreAuthorize("hasAuthority('TRADE_READ')")` will never work.

**Fix:** Configure the converter to read your actual permission claim:
```java
public ReactiveJwtAuthenticationConverter() {
    JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
    JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
    gac.setAuthoritiesClaimName("permissions");
    gac.setAuthorityPrefix("");
    delegate.setJwtGrantedAuthoritiesConverter(gac);
    super(delegate);
}
```

---

### [HIGH] S-7 — ValidationError exposes raw `rejectedValue` Object — potential info leak
**File:** `srta-common-api/ValidationError.java:10`

```java
Object rejectedValue;
```

If a domain object, password, or internal ID is rejected, the full object (including all its fields) is serialized into the 400 response and sent to the client. This can leak sensitive data.

**Fix:** Convert to `String` and apply sanitization at build time:
```java
String rejectedValue; // safe string representation only
```
In `GlobalWebExceptionHandler`, use `String.valueOf(error.getRejectedValue())` when building `ValidationError`.

---

### [MEDIUM] S-8 — Correlation ID header not sanitized — header injection risk
**File:** `srta-common-web/CorrelationIdWebFilter.java:30-32`

```java
String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
if (correlationId == null || correlationId.isEmpty()) {
    correlationId = UUID.randomUUID().toString();
}
```

A client can inject arbitrary characters (e.g., CRLF `\r\n`) into `X-Correlation-ID`, which then gets written into the response headers and logs, enabling header injection and log forging.

**Fix:**
```java
private static final Pattern SAFE_CORRELATION_ID = Pattern.compile("^[a-zA-Z0-9\\-]{8,64}$");

String raw = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
String correlationId = (raw != null && SAFE_CORRELATION_ID.matcher(raw).matches())
    ? raw
    : UUID.randomUUID().toString();
```

---

### [MEDIUM] S-9 — GitHub Actions uses a custom secret instead of the built-in GITHUB_TOKEN
**File:** `.github/workflows/publish_library.yml:31`

```yaml
GITHUB_TOKEN: ${{ secrets.GH_PASSWORD }}
```

`GH_PASSWORD` implies a personal access token stored as a secret. If this token has broad repo scope it can be leaked via a compromised workflow. The built-in `GITHUB_TOKEN` is scoped to this workflow run.

Also, `GITHUB_ACTOR` is used in Gradle credentials but is **never set** in the env block — it works by accident only because it's a built-in env var.

**Fix:**
```yaml
env:
  GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
  GITHUB_ACTOR: ${{ github.actor }}
```
Upgrade `actions/checkout@v2` → `actions/checkout@v4`.

---

### [LOW] S-10 — @RequiresPermission annotation has no enforcing aspect
**File:** `srta-common-security/RequiresPermission.java`

The annotation is defined but no `@Aspect`, `MethodInterceptor`, or `@PreAuthorize` processor enforces it. Any method annotated with `@RequiresPermission` has **no actual protection** — it's a documentation annotation only.

**Fix:** Either:
1. Delete the annotation and use `@PreAuthorize("hasAuthority('{value}')")` directly, OR
2. Implement an AOP-based `PermissionEnforcingAspect` that reads the annotation and delegates to Spring Security's `ReactiveAuthorizationManager`.

---

## 2. LOGIC BUGS

### [HIGH] L-1 — AuditPublisher catches JsonProcessingException outside Mono pipeline
**File:** `srta-common-audit/AuditPublisher.java:30-36`

```java
public Mono<Void> publish(AuditEvent event) {
    try {
        byte[] body = objectMapper.writeValueAsBytes(event); // eager — throws outside Mono
        ...
    } catch (JsonProcessingException e) {
        return Mono.error(e); // fine, but still not lazy
    }
}
```

`objectMapper.writeValueAsBytes()` is called eagerly in the constructor of the `Mono` chain, not inside the reactive pipeline. This is consistent but non-idiomatic and prevents retry operators from re-serializing. The catch block is the only error signal path.

**Fix:** Wrap in `Mono.fromCallable` to make serialization lazy and retryable:
```java
public Mono<Void> publish(AuditEvent event) {
    return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(event))
        .flatMap(body -> sender.send(Mono.just(new OutboundMessage(EXCHANGE_NAME, ROUTING_KEY, body))))
        .doOnSuccess(v -> log.debug("Audit event published: {}", event.getEventType()))
        .doOnError(e -> log.error("Failed to publish audit event: {}", event.getEventType(), e));
}
```

---

### [HIGH] L-2 — PagedResponse.of() throws ArithmeticException when size = 0
**File:** `srta-common-api/PagedResponse.java:22`

```java
int totalPages = (int) Math.ceil((double) totalElements / size); // divide by zero if size=0
```

If a caller accidentally passes `size=0`, this throws `ArithmeticException` (integer division by zero, as the cast overflows). `totalPages=0` combined with `page=0` also causes `last` to evaluate as `0 >= -1 = true` — the only page IS the last page, which may not be intended.

**Fix:**
```java
public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
    if (size <= 0) throw new IllegalArgumentException("Page size must be > 0, got: " + size);
    int totalPages = (int) Math.ceil((double) totalElements / size);
    return PagedResponse.<T>builder()
        .content(content != null ? content : List.of())
        .page(page)
        .size(size)
        .totalElements(totalElements)
        .totalPages(totalPages)
        .last(totalPages == 0 || page >= totalPages - 1)
        .build();
}
```

---

### [MEDIUM] L-3 — JwtParser silently falls back from "permissions" to "roles"
**File:** `srta-common-security/JwtParser.java:22-26`

```java
List<String> permissions = jwt.getClaimAsStringList("permissions");
if (permissions == null) {
    permissions = jwt.getClaimAsStringList("roles"); // silent fallback
}
```

If neither claim exists, `Mono.justOrEmpty(null)` returns `Mono.empty()`, which downstream operators treat as "no permissions" — silently granting no access without any warning. The fallback also conflates two different authorization models.

**Fix:** Log a warning when falling back, and emit `Mono.empty()` intentionally:
```java
public Mono<List<String>> getPermissions(Jwt jwt) {
    List<String> permissions = jwt.getClaimAsStringList("permissions");
    if (permissions != null) return Mono.just(permissions);
    List<String> roles = jwt.getClaimAsStringList("roles");
    if (roles != null) {
        log.warn("JWT missing 'permissions' claim; falling back to 'roles' for subject={}", jwt.getSubject());
        return Mono.just(roles);
    }
    log.warn("JWT has neither 'permissions' nor 'roles' claim for subject={}", jwt.getSubject());
    return Mono.empty();
}
```

---

### [MEDIUM] L-4 — BaseEntity.createdBy and updatedBy are never automatically populated
**File:** `srta-common-domain/BaseEntity.java`

`@CreatedDate` and `@LastModifiedDate` are honored by Spring Data's auditing infrastructure, but `createdBy` / `updatedBy` are plain fields with **no `@CreatedBy` / `@LastModifiedBy` annotations**. They will always be `null` unless services manually set them.

**Fix:**
```java
@CreatedBy
private String createdBy;

@LastModifiedBy
private String updatedBy;
```
And configure `ReactiveAuditorAware<String>` in a `@EnableR2dbcAuditing` configuration bean.

---

## 3. VALIDATION GAPS

### [HIGH] V-1 — AuditEvent fields have no validation constraints
**File:** `srta-common-audit/AuditEvent.java`

All fields are nullable. A consumer can publish an `AuditEvent` with `eventType=null`, `userId=null`, etc., producing silent garbage in the audit log.

**Fix:** Add JSR-380 annotations:
```java
@NotBlank String eventType;
@NotBlank String userId;
@NotNull  LocalDateTime timestamp;
// correlationId and ipAddress can remain optional
```
Validate in `AuditPublisher.publish()`:
```java
// Add jakarta.validation dependency and call Validator.validate(event) before publishing
```

---

### [HIGH] V-2 — ValidationError.field can be null but is never documented or guarded
**File:** `srta-common-api/ValidationError.java` / `GlobalWebExceptionHandler.java:49`

`FieldError.getField()` can return `null` for class-level `@ScriptAssert` or cross-field constraint violations. If `field` is null, clients receive `{"field": null, ...}` which breaks client-side error mapping.

**Fix:**
```java
.field(error.getField() != null ? error.getField() : "_global")
```

---

### [MEDIUM] V-3 — ApiResponse.timestamp uses LocalDateTime — no timezone context
**File:** `srta-common-api/ApiResponse.java:16`

```java
LocalDateTime timestamp = LocalDateTime.now();
```

`LocalDateTime` has no timezone. In a distributed deployment across zones, timestamps are ambiguous and cannot be reliably sorted or compared by consumers.

**Fix:** Use `Instant`:
```java
@Builder.Default
Instant timestamp = Instant.now();
```
This is zone-agnostic and sortable. Update Jackson config to serialize as ISO-8601 epoch strings.

---

### [LOW] V-4 — PagedResponse has no validation that page is non-negative
**File:** `srta-common-api/PagedResponse.java`

A negative `page` index produces `last = true` (e.g., `page=-1 >= totalPages-1` when `totalPages=1`), which is semantically wrong.

**Fix:** Add a guard in `of()`:
```java
if (page < 0) throw new IllegalArgumentException("Page index must be >= 0");
```

---

## 4. API DESIGN ISSUES

### [HIGH] A-1 — PagedResponse is not wrapped in ApiResponse — inconsistent envelope
**File:** `srta-common-api/PagedResponse.java`

All single-entity responses use `ApiResponse<T>`. But `PagedResponse<T>` is a parallel structure that returns content directly without the standard `success/code/message` envelope. Consumer clients must handle two different response shapes.

**Fix:** Remove the `PagedResponse` top-level wrapper and instead use:
```java
ApiResponse<PagedResponse<T>> // consistent envelope for all responses
```
Or define `PagedResponse` as a data class that is placed inside `ApiResponse.data`:
```java
ApiResponse.success(PagedResponse.of(content, page, size, total))
```

---

### [MEDIUM] A-2 — ApiResponse.error(ErrorCode, String) allows overriding the canonical message
**File:** `srta-common-api/ApiResponse.java:57`

The overload `error(ErrorCode errorCode, String message)` lets callers replace the standardized `ErrorCode.message` with any string. This breaks the contract that `code` → `message` is canonical and breaks client i18n strategies that map error codes to localized messages.

**Fix:** Remove the overload. If extra context is needed, add a dedicated `detail` field to `ApiResponse`:
```java
String detail; // optional, non-canonical extra context
```
Use `ApiResponse.error(ErrorCode.FORBIDDEN).withDetail("Requires TRADE_APPROVE permission")`.

---

### [MEDIUM] A-3 — GlobalWebExceptionHandler uses ResponseEntity instead of ServerResponse
**File:** `srta-common-web/GlobalWebExceptionHandler.java`

The handler extends `@RestControllerAdvice` and returns `ResponseEntity<>`. In WebFlux this works, but for non-controller error paths (e.g., filter-level exceptions, `RouterFunction` routes), `@RestControllerAdvice` is **not invoked**. Filter errors fall through to the default Spring `DefaultErrorWebExceptionHandler`.

**Fix:** Implement `WebExceptionHandler` (the correct WebFlux error handling contract) in addition to `@RestControllerAdvice`, or replace with a `@Component` that implements `ErrorWebExceptionHandler` at order `-2` (before the default).

---

### [LOW] A-4 — ErrorCode enum mixes error categories without grouping
**File:** `srta-common-api/ErrorCode.java`

44 codes in a flat enum. As more services are added, this will grow unmanageable and merging changes across teams will cause conflicts.

**Fix:** Either segment by category with a prefix convention (`AUTH_*`, `TRADE_*`, `CBS_*`), or split into module-specific enums that implement a common `AppErrorCode` interface.

---

## 5. TEST COVERAGE GAPS

### [HIGH] T-1 — ArchUnit rules use `allowEmptyShould(true)` — rules can silently pass with zero matches
**File:** `srta-common-web/ArchitectureRulesTest.java:20,26,34`

```java
.allowEmptyShould(true) // silently passes if nothing matches the pattern
```

If packages are renamed, class annotations change, or the codebase evolves, these rules pass vacuously without catching violations. This defeats the purpose of architecture enforcement.

**Fix:** Change all rules to `.allowEmptyShould(false)`. Add at least one real class in each targeted package so rules are tested against real code, not empty sets.

---

### [HIGH] T-2 — No tests for CorrelationIdWebFilter
**File:** `srta-common-web` — missing test

The filter is a critical cross-cutting concern touching every request. No test verifies:
- Correlation ID is forwarded from request to response
- Missing header generates a UUID
- Injected header values are sanitized
- Reactor Context receives the correlation ID

**Fix:** Add `CorrelationIdWebFilterTest` using `WebTestClient`:
```java
WebTestClient.bindToController(new NoOpController())
    .webFilter(new CorrelationIdWebFilter())
    .build()
    .get().uri("/")
    .exchange()
    .expectHeader().exists("X-Correlation-ID");
```

---

### [HIGH] T-3 — No tests for SecurityContextHelper or JwtParser
**Files:** `srta-common-security` — no test directory

Critical security utilities with zero test coverage. `getCurrentUserId()` returning empty or wrong values would silently pass the wrong user ID to downstream services.

**Fix:** Add tests using `WithMockJwt` or `JwtAuthenticationToken` populated with a stub `Jwt`:
```java
@Test
void getCurrentUserId_returnsSubjectClaim() {
    Jwt jwt = Jwt.withTokenValue("token").header("alg","none")
        .subject("user-42").build();
    JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);
    StepVerifier.create(
        SecurityContextHelper.getCurrentUserId()
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
    ).expectNext("user-42").verifyComplete();
}
```

---

### [HIGH] T-4 — AuditPublisher has no test
**File:** `srta-common-audit` — no test directory

The RabbitMQ publish path is entirely untested. A serialization regression or routing key change would only surface in production.

**Fix:** Add `AuditPublisherTest` using a mock `Sender`:
```java
@Test
void publish_sendsMessageToCorrectExchangeAndRoutingKey() {
    Sender sender = mock(Sender.class);
    when(sender.send(any())).thenReturn(Mono.empty());
    AuditPublisher publisher = new AuditPublisher(sender, new ObjectMapper());
    AuditEvent event = AuditEvent.builder()...build();
    StepVerifier.create(publisher.publish(event)).verifyComplete();
    verify(sender).send(argThat(msgs -> /* check exchange/routing key */));
}
```

---

### [MEDIUM] T-5 — GlobalWebExceptionHandlerTest does not test WebExchangeBindException handler
**File:** `srta-common-web/GlobalWebExceptionHandlerTest.java`

The `handleValidationException` method is the most complex handler and is completely untested. Field errors, null fields, and null rejected values are not covered.

**Fix:** Add:
```java
@Test
void handleValidationException_returnsFieldErrors() {
    WebExchangeBindException ex = // mock or construct with FieldError list
    ResponseEntity<ApiResponse<Void>> response = handler.handleValidationException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertFalse(response.getBody().getErrors().isEmpty());
}
```

---

### [MEDIUM] T-6 — ApiResponse and PagedResponse factory methods are not tested
**File:** `srta-common-api` — no test directory

Edge cases not covered:
- `PagedResponse.of()` when `totalElements = 0`
- `PagedResponse.of()` when `page = totalPages - 1` (last page detection)
- `ApiResponse.success(null)` — should be legal
- `ApiResponse.validationError(emptyList())` — should fail or be guarded

---

## 6. PRODUCTION-READINESS IMPROVEMENTS

### [CRITICAL] P-1 — No Spring Boot auto-configuration — library cannot be consumed as a starter
**File:** All modules — missing `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

The modules define `@Component`, `@Configuration`, and `@RestControllerAdvice` beans, but there is no `AutoConfiguration.imports` file (Spring Boot 3.x+) or `spring.factories` (Spring Boot 2.x). Consumers who import the JAR must manually `@Import` or `@ComponentScan` every bean — this is fragile and defeats the purpose of a shared library.

**Fix:** Add for each module (e.g., `srta-common-web`):
```
# src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
org.meldtech.platform.srta.common.web.WebAutoConfiguration
```
And create `WebAutoConfiguration.java`:
```java
@AutoConfiguration
@Import({GlobalWebExceptionHandler.class, CorrelationIdWebFilter.class, OpenApiConfig.class})
public class WebAutoConfiguration {}
```

---

### [HIGH] P-2 — AuditPublisher has no resilience — RabbitMQ failure silently breaks audit trail
**File:** `srta-common-audit/AuditPublisher.java`

If RabbitMQ is down, `publish()` returns a failing `Mono`. Callers that don't handle this error will propagate a 500 to the user for what should be a non-fatal background operation.

**Fix:** Add a fire-and-forget fallback with error logging:
```java
public Mono<Void> publish(AuditEvent event) {
    return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(event))
        .flatMap(body -> sender.send(Mono.just(new OutboundMessage(EXCHANGE_NAME, ROUTING_KEY, body))))
        .onErrorResume(e -> {
            log.error("AUDIT PUBLISH FAILED — event={} error={}", event.getEventType(), e.getMessage());
            return Mono.empty(); // degrade gracefully; audit loss is preferable to 500
        });
}
```
For zero-loss audit requirements, add a local outbox table fallback.

---

### [HIGH] P-3 — spring-boot-starter-amqp and reactor-rabbitmq both included — dependency conflict
**File:** `build.gradle` (srta-common-audit block)

```gradle
implementation 'org.springframework.boot:spring-boot-starter-amqp'  // blocking AMQP
implementation 'io.projectreactor.rabbitmq:reactor-rabbitmq:1.5.6'  // reactive AMQP
```

`spring-boot-starter-amqp` pulls in the blocking `RabbitTemplate` and auto-configures `CachingConnectionFactory`. `reactor-rabbitmq` requires a separate `ReactorRabbitMq` connection factory. Having both creates redundant connections and conflicting auto-configurations.

**Fix:** Remove `spring-boot-starter-amqp` from the audit module. Configure `reactor-rabbitmq` directly:
```gradle
implementation 'io.projectreactor.rabbitmq:reactor-rabbitmq:1.5.6'
implementation 'io.projectreactor.netty:reactor-netty' // needed for reactor-rabbitmq
```

---

### [HIGH] P-4 — No @Order on CorrelationIdWebFilter — ordering relative to security is undefined
**File:** `srta-common-web/CorrelationIdWebFilter.java`

Security filters run at `SecurityWebFiltersOrder.AUTHENTICATION` (order ~100). If `CorrelationIdWebFilter` runs after authentication, failed auth requests will have no correlation ID in their logs.

**Fix:** Set explicit order to run before security:
```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter { ... }
```

---

### [MEDIUM] P-5 — No @ConditionalOnMissingBean on shared beans — breaks consumer customization
**Files:** `AuditPublisher.java`, `JwtParser.java`, `CorrelationIdWebFilter.java`, `OpenApiConfig.java`

All auto-configured beans are `@Component` / `@Configuration` without `@ConditionalOnMissingBean`. A consumer that wants to override `JwtParser` with a custom version will get a `NoUniqueBeanDefinitionException` or have the library override their custom bean.

**Fix:** Annotate auto-configured beans:
```java
@Bean
@ConditionalOnMissingBean
public JwtParser jwtParser() { return new JwtParser(); }
```

---

### [MEDIUM] P-6 — GitHub Actions workflow uses pinned action SHA for Gradle but not for checkout/setup-java
**File:** `.github/workflows/publish_library.yml`

`actions/checkout@v2` and `actions/setup-java@v4` are referenced by mutable tags. Tags can be re-pointed to malicious commits. Only `gradle/actions/setup-gradle` is pinned to a SHA.

**Fix:** Pin all actions to a full commit SHA:
```yaml
uses: actions/checkout@11bd71901bbe5b1630ceea73d27597364c9af683 # v4.2.2
uses: actions/setup-java@c5195efecf7bdfc987ee8bae7a71cb8b11521c00 # v4.7.0
```

---

### [LOW] P-7 — Spring Boot version 4.0.5 — verify release status
**File:** `build.gradle:4`

```gradle
id 'org.springframework.boot' version '4.0.5' apply false
```

Spring Boot 4.x is a future/milestone release (Spring Boot 3.x is the current stable line as of this review). Using a milestone or snapshot in a shared library that will be consumed by production services is risky.

**Action:** Confirm this is intentional and that all downstream services are aligned. If 4.x is pre-GA, pin to the latest 3.x stable release until 4.x reaches GA.

---

### [LOW] P-8 — No actuator or health check configuration
**File:** `srta-common-platform/application.properties`

The platform module (and by extension any service using this library) exposes no health or readiness endpoints. Kubernetes liveness/readiness probes will have nothing to call.

**Fix:** Add to `application.properties`:
```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.probes.enabled=true
```
And add `spring-boot-starter-actuator` as an `api` dependency in the platform BOM.

---

## SUMMARY TABLE

| ID   | Severity | Category          | File / Module                         | One-Line Description                                      |
|------|----------|-------------------|---------------------------------------|-----------------------------------------------------------|
| S-1  | CRITICAL | Security          | GlobalWebExceptionHandler             | NOT_FOUND → INTERNAL_ERROR wrong mapping                  |
| S-2  | CRITICAL | Security          | GlobalWebExceptionHandler             | CONFLICT → USERNAME_EXISTS leaks domain logic             |
| S-3  | CRITICAL | Security          | GlobalWebExceptionHandler             | SERVICE_UNAVAILABLE → CBS_UNAVAILABLE always wrong        |
| S-4  | HIGH     | Security          | CorrelationIdWebFilter                | MDC.put() in reactive streams is thread-unsafe            |
| S-5  | HIGH     | Security          | srta-common-security (missing)        | No SecurityFilterChain bean — exposes login page          |
| S-6  | HIGH     | Security          | ReactiveJwtAuthenticationConverter    | No authority mapping — @PreAuthorize always fails         |
| S-7  | HIGH     | Security          | ValidationError                       | Raw Object rejectedValue leaks internal data              |
| S-8  | MEDIUM   | Security          | CorrelationIdWebFilter                | Unsanitized correlation ID enables header injection       |
| S-9  | MEDIUM   | Security          | publish_library.yml                   | GH_PASSWORD secret; checkout@v2 mutable tag               |
| S-10 | LOW      | Security          | RequiresPermission                    | Annotation is decorative — no enforcement aspect          |
| L-1  | HIGH     | Logic Bug         | AuditPublisher                        | Serialization outside Mono pipeline — not lazy            |
| L-2  | HIGH     | Logic Bug         | PagedResponse                         | Divide by zero when size=0                                |
| L-3  | MEDIUM   | Logic Bug         | JwtParser                             | Silent permissions→roles fallback; no warning             |
| L-4  | MEDIUM   | Logic Bug         | BaseEntity                            | createdBy/updatedBy never auto-populated                  |
| V-1  | HIGH     | Validation        | AuditEvent                            | No @NotBlank on required audit fields                     |
| V-2  | HIGH     | Validation        | ValidationError + handler             | Null field possible for cross-field constraint violations |
| V-3  | MEDIUM   | Validation        | ApiResponse                           | LocalDateTime has no timezone — use Instant               |
| V-4  | LOW      | Validation        | PagedResponse                         | Negative page index not guarded                           |
| A-1  | HIGH     | API Design        | PagedResponse                         | Inconsistent envelope — not wrapped in ApiResponse        |
| A-2  | MEDIUM   | API Design        | ApiResponse                           | error(code, msg) overload breaks canonical code→message   |
| A-3  | MEDIUM   | API Design        | GlobalWebExceptionHandler             | @RestControllerAdvice misses filter-level exceptions      |
| A-4  | LOW      | API Design        | ErrorCode                             | Flat 44-code enum — no grouping or namespacing            |
| T-1  | HIGH     | Test Coverage     | ArchitectureRulesTest                 | allowEmptyShould(true) — rules silently vacuous           |
| T-2  | HIGH     | Test Coverage     | srta-common-web (missing)             | No tests for CorrelationIdWebFilter                       |
| T-3  | HIGH     | Test Coverage     | srta-common-security (missing)        | No tests for SecurityContextHelper / JwtParser            |
| T-4  | HIGH     | Test Coverage     | srta-common-audit (missing)           | No tests for AuditPublisher                               |
| T-5  | MEDIUM   | Test Coverage     | GlobalWebExceptionHandlerTest         | WebExchangeBindException handler untested                 |
| T-6  | MEDIUM   | Test Coverage     | srta-common-api (missing)             | ApiResponse and PagedResponse edge cases untested         |
| P-1  | CRITICAL | Prod Readiness    | All modules (missing)                 | No AutoConfiguration.imports — library cannot self-register |
| P-2  | HIGH     | Prod Readiness    | AuditPublisher                        | No resilience — RabbitMQ failure propagates as 500        |
| P-3  | HIGH     | Prod Readiness    | build.gradle (audit)                  | spring-amqp + reactor-rabbitmq conflict                   |
| P-4  | HIGH     | Prod Readiness    | CorrelationIdWebFilter                | No @Order — may run after security filters                |
| P-5  | MEDIUM   | Prod Readiness    | All @Component beans                  | No @ConditionalOnMissingBean — blocks consumer overrides  |
| P-6  | MEDIUM   | Prod Readiness    | publish_library.yml                   | Mutable action tags — supply chain risk                   |
| P-7  | LOW      | Prod Readiness    | build.gradle                          | Spring Boot 4.0.5 — verify GA status                     |
| P-8  | LOW      | Prod Readiness    | application.properties                | No actuator/health config for K8s probes                  |

---

## RECOMMENDED PRIORITY ORDER

1. **P-1** — Add AutoConfiguration.imports (library is currently not usable as a starter)
2. **S-5** — Add SecurityFilterChain default (any consumer is insecure without it)
3. **S-6** — Fix authority mapping (all @PreAuthorize checks are broken)
4. **S-1, S-2, S-3** — Fix exception handler status mappings (incorrect HTTP semantics)
5. **S-4** — Fix MDC threading (broken correlation in all logs)
6. **L-2** — Guard PagedResponse.of() size=0 (runtime exception in production)
7. **T-1, T-2, T-3, T-4** — Restore meaningful test coverage
8. **P-2, P-3** — Fix AuditPublisher resilience and dependency conflict
9. Everything else in order of severity