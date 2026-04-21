# srta-common

Shared reactive Java libraries for SRTA services, organized as a multi-module Gradle project.

## Overview

This repository provides reusable common modules for backend services, including:
- API response models and error contracts
- Security helpers for reactive authentication/authorization
- WebFlux-level cross-cutting web components (filters, exception handling, OpenAPI config)
- Shared domain base types and enums
- Audit abstractions
- A platform/BOM module to align dependency versions across modules

## Tech Stack

- **Language**: Java 21
- **Build tool / package manager**: Gradle (via wrapper)
- **Frameworks/Libraries**:
  - Spring Boot `4.0.5`
  - Spring WebFlux
  - Spring Security + OAuth2 Resource Server
  - Spring Data R2DBC (in `srta-common-domain`)
  - Reactor + Reactor Test
  - Springdoc OpenAPI WebFlux UI
  - JUnit 5 + ArchUnit + Mockito (via Spring Boot test starter)

## Requirements

- JDK 21+
- Gradle Wrapper (included)
  - Wrapper scripts: `./gradlew` (macOS/Linux), `gradlew.bat` (Windows)

## Setup

Clone and build:

```bash
git clone <repo-url>
cd srta-common
./gradlew build
```

> TODO: Replace `<repo-url>` with your canonical repository URL.

## Run / Entry Points

This repository is primarily a **library project** (most modules publish JARs, not standalone apps).

Main application entry point found:
- `srta-common-platform/src/main/java/org/meldtech/platform/srta/common/SrtaCommonPlatformApplication.java`

Run it locally (if intended to be executable in your environment):

```bash
./gradlew :srta-common-platform:bootRun
```

If you only need library artifacts:

```bash
./gradlew build
```

## Scripts / Common Commands

### Build, test, package all modules

```bash
./gradlew build
```

### Run all tests

```bash
./gradlew test
```

### Run a specific test class

```bash
./gradlew test --tests org.meldtech.platform.srta.common.security.JwtParserTest
```

### Publish artifacts (CI/release flow)

Release workflow publishes on git tag push (`v*`) via:

```bash
./gradlew -Pversion=<tag-version> publish
```

## Environment Variables

The following environment variables are used for publishing to GitHub Packages:

- `GITHUB_ACTOR` — package registry username
- `GITHUB_TOKEN` — package registry token/password (in CI mapped from `secrets.GH_PASSWORD`)

No additional runtime env vars are explicitly defined in this repository’s default `application.properties`.

> TODO: Add service-specific runtime env vars if downstream services require extra configuration.

## Testing

Frameworks in use:
- JUnit 5 (Jupiter)
- Reactor Test (`StepVerifier` for reactive flows)

Commands:

```bash
./gradlew test
./gradlew test --tests <FullClassName>
```

Notable test locations:
- `srta-common-api/src/test/java`
- `srta-common-audit/src/test/java`
- `srta-common-security/src/test/java`
- `srta-common-web/src/test/java`
- `srta-common-platform/src/test/java`

## Project Structure

```text
srta-common/
├── build.gradle
├── settings.gradle
├── srta-common-api/        # API models/contracts (ApiResponse, ErrorCode, etc.)
├── srta-common-security/   # Reactive security helpers and auto-configuration
├── srta-common-audit/      # Audit event and publisher components
├── srta-common-domain/     # Shared domain base entity/enums
├── srta-common-web/        # WebFlux filters, exception handlers, OpenAPI config
└── srta-common-platform/   # Java platform/BOM + Spring Boot application class
```

## License

> TODO: Add the project license and include a root `LICENSE` file.

Current repository does not contain a dedicated project license file.
