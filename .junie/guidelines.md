# Project Guidelines: srta-common

## Build/Configuration Instructions
- **Prerequisites**: Java 21+ and Gradle 9.4.0+ (via Gradle Wrapper).
- **Build**: Use `./gradlew build` to compile, test, and package the application.
- **Dependencies**: The project uses Spring Boot 4.0.5 with WebFlux. All dependencies are managed in `build.gradle`.

## Testing Information
### Configuration
- Tests use **JUnit 5 (Jupiter)** and **Reactor Test**.
- To run all tests: `./gradlew test`
- To run a specific test: `./gradlew test --tests <FullClassName>`

### Guidelines for New Tests
1. **Reactive Testing**: Use `StepVerifier` for testing `Mono<T>` and `Flux<T>`. This ensures proper subscription and assertion of the reactive stream behavior.
2. **Context Loads**: Every service/module should maintain at least one `@SpringBootTest` to ensure the Spring context loads correctly.
3. **Mocking**: Use `Mockito` for external dependencies, but prefer integration tests for core logic where feasible.

### Example Test (Reactive)
```java
package org.meldtech.platform.srta.common;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ReactiveTestDemo {
    @Test
    void testSimpleMono() {
        Mono<String> mono = Mono.just("Hello, WebFlux!");
        
        StepVerifier.create(mono)
                .expectNext("Hello, WebFlux!")
                .verifyComplete();
    }
}
```

## Additional Development Information
- **Code Style**: Follow standard Java and Spring conventions. Use 4 spaces for indentation.
- **Reactive Stack**: 
    - **Spring WebFlux**: Ensure all controllers, services, and repositories return `Mono<T>` or `Flux<T>`.
    - **Avoid Blocking**: Never use `.block()` or `toStream().toList()` in production code. Use reactive operators (`flatMap`, `switchIfEmpty`, etc.) to handle data flows.
    - **R2DBC**: Use Spring Data R2DBC for database operations. Ensure database interaction is non-blocking throughout the stack.
- **Project Structure**: Standard Maven/Gradle directory layout (`src/main/java`, `src/main/resources`).
- **Debugging**: Enable debug logging for Reactor in development if needed by setting `logging.level.reactor.netty=DEBUG`.

## Spring WebFlux Best Practice
- **Pipeline Integrity**: Maintain the reactive chain from controller down to the database.
- **Error Handling**: Use `onErrorResume`, `onErrorReturn`, or global `@ControllerAdvice` for consistent error management.
- **Immutability**: Prefer immutable data objects (Records or Final classes) for state management within reactive streams.
