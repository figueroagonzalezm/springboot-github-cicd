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

- `.github/workflows/ci.yml` — the caller workflow, triggered on `push` (main + `v*` tags), `pull_request`, and `workflow_dispatch`. Has three jobs:
  - `build-and-test` — invokes the reusable `build-and-test.yml` workflow (Day 2) via `workflow_call`, parameterized by `java-version`/`gradle-task`.
  - `build-and-push-image` (Day 3) — builds `build/libs/app.jar`, then builds/pushes the Docker image to ECR via Buildx (`docker/build-push-action`), tagged with `sha-<commit>`, the bare full commit SHA (for `deploy-to-eks` to consume unambiguously), `latest` (default branch only), and semver (on `v*` tags). Authenticates to AWS via OIDC (`aws-actions/configure-aws-credentials`, `id-token: write`) — no static AWS credentials. Skipped on `pull_request` events. Depends on repo/org variables (`vars.AWS_REGION`, `vars.ECR_REPOSITORY`, `vars.AWS_ROLE_ARN`) provisioned by the `iam` Terraform stack in Day 4.
  - `deploy-to-eks` (Day 5) — needs `build-and-push-image`; only runs on `push` to `main` (not tags, not `pull_request`). Runs inside the `production` GitHub Environment because the `eks` Terraform stack's `github-actions-deployer` role trusts that Environment specifically (`sub` conditioned on `repo:<repo>:environment:production`) — a different, broader role than `build-and-push-image`'s `AWS_ROLE_ARN`. The `production` Environment also has a required-reviewers protection rule (added ahead of Day 6, via the API — not in any YAML file): the job queues automatically on every push to main but pauses for manual approval in the Actions UI before it does anything. Once approved, it authenticates via OIDC, runs `aws eks update-kubeconfig`, rewrites the image tag in `k8s/overlays/production` with `kustomize edit set image` (bundled on `ubuntu-latest`, alongside `kubectl`), applies with `kubectl apply -k`, then `kubectl rollout status`. Depends on `vars.EKS_CLUSTER_NAME` and `vars.AWS_EKS_DEPLOY_ROLE_ARN`, which don't exist until the `eks` stack is applied — expect this job to fail until then.
- `.github/workflows/build-and-test.yml` — the Day 2 reusable workflow (`workflow_call`): builds and tests with Gradle, caches `~/.gradle/caches`/`~/.gradle/wrapper`, publishes the JUnit report as an artifact.
- Both workflow files are commented inline to explain each GitHub Actions concept (events, jobs/runners, caching keys, artifacts, OIDC, reusable workflows) for learning purposes — keep those comments when editing.
- `k8s/` (Day 5) — Kustomize manifests for the app: `k8s/base/` has the `Deployment` (2 replicas, readiness/liveness probes against `GET /employees`, resources sized for Fargate), `Service` (`ClusterIP`; no Ingress/load balancer is provisioned, so reach the app via `kubectl port-forward`), and a `ConfigMap` supplying `JAVA_OPTS` (read by the Dockerfile's entrypoint). `k8s/overlays/production/` sets `namespace: app` (matching the `eks` Terraform stack's `application_namespace`) and holds a placeholder `images:` entry that `deploy-to-eks` rewrites per deploy. There's only a `production` overlay so far — Day 6 is where `dev`/`staging` Environments get formalized; add corresponding overlays then, don't invent them ahead of that.
- Later days in the plan add: `dev`/`staging` Environments and a Trivy scanning quality gate (Day 6 — the `production` approval gate itself is already in place) and a second microservice reusing this pattern (Day 7).
