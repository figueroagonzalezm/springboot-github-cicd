# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose of this repo

This is a learning project for practicing GitHub Actions CI/CD into AWS (ECR + EKS), following the day-by-day curriculum in `plan-github-actions-cicd-spring-eks.md`. The Spring Boot app itself (an in-memory Employee CRUD API) is a vehicle for that pipeline work, not the focus — check that plan file for which day's objectives are currently active before assuming a change is out of scope.

## Commands

Use the Gradle wrapper, not a globally installed Gradle. On Windows use `gradlew.bat`; the `./gradlew` form below is what CI (`.github/workflows/ci.yml`) uses on Linux runners.

```
./gradlew build              # compile, run tests, package the jar
./gradlew test                # run all tests
./gradlew test --tests "org.example.springboot.EmployeeControllerTest"                 # single test class
./gradlew test --tests "org.example.springboot.EmployeeControllerTest.methodName"       # single test method
./gradlew bootRun            # run the app locally (port 8080)
./gradlew bootJar            # produces build/libs/app.jar (archiveFileName is pinned in build.gradle)
```

`docker build .` expects `build/libs/app.jar` to already exist — run `./gradlew build` (or `bootJar`) before building the image; the Dockerfile does not run Gradle itself.

## Architecture

Single-package Spring Boot app (`org.example.springboot`) with a straightforward layered structure:

- `EmployeeController` — REST endpoints under `/employees` (GET/POST/PUT/DELETE), does its own request validation (required fields, email format) and returns a plain `ErrorResponse` record on 400/404 rather than using `@Valid`/exception handlers.
- `EmployeeService` — thin pass-through to the repository.
- `EmployeeRepository` — **in-memory only** (`ArrayList`, `@Repository`); there is no database. Data resets on every restart and IDs are assigned by `max(existing) + 1`. `clear()` exists specifically for test setup (`@BeforeEach` in `EmployeeControllerTest`).
- `Employee`, `CreateEmployeeRequest`, `UpdateEmployeeRequest` — Java records.
- `ApiDocsController` — hand-written OpenAPI 3.0 document (`/v3/api-docs`) and a hand-written HTML page (`/swagger-ui.html`, `/swagger-ui/index.html`) with an inline JS form for calling the API; this is not springdoc/swagger-generated, so if endpoints or schemas change, this file must be updated by hand to stay in sync.

Naming note: the Gradle root project is `springboot`, the main application class is `SpringBootAwsCicdApplication`, and the Maven-style group is `org.example` — these don't follow a single consistent name, which is expected in this project (not a bug to "fix").

There are two near-identical trivial `@SpringBootTest` context-load tests (`SpringbootApplicationTests` and `SpringBootAwsCicdApplicationTests`); treat both as intentional leftovers rather than something to deduplicate without being asked.

## CI/CD

- `.github/workflows/ci.yml` — the Day 1 workflow: builds and tests on `push`/`pull_request`/`workflow_dispatch`, caches `~/.gradle/caches` and `~/.gradle/wrapper`, and publishes the JUnit report as an artifact. It's commented inline to explain each GitHub Actions concept (events, jobs/runners, caching keys, artifacts) for learning purposes — keep those comments when editing.
- Later days in the plan add: a reusable `workflow_call` workflow, building/pushing images to ECR via OIDC (no static AWS credentials), Terraform-provisioned IAM/OIDC resources (in the separate `C:\dev\learning\terraform` project), and CD to EKS via Kustomize.
