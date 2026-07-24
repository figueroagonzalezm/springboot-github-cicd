# Intensive 1-Week Plan: GitHub Actions for CI/CD of Spring Boot Microservices on AWS (ECR + EKS)

**Starting level:** you already have solid experience with Docker, Kubernetes, and AWS. This plan doesn't explain those from scratch; it focuses on GitHub Actions and how to use them to orchestrate the full CI/CD cycle through to EKS.

**Build tool:** Gradle.

**Existing Terraform project:** `C:\dev\learning\terraform` — reused starting Day 4 to provision ECR, the GitHub OIDC provider, and the IAM role the pipeline will use.

**How to use this plan with Claude Code:** each workshop includes a "Prompt for Claude Code" section with the exact request you can paste into your terminal to have it help you build that day's files.

---

## Day 1 — GitHub Actions Fundamentals

**Objectives:** events (`push`, `pull_request`, `workflow_dispatch`), `jobs`/`steps` structure, runners, contexts (`github.*`, `secrets.*`, `env.*`), artifacts, caching (`actions/cache` for the Gradle cache and wrapper).

**Practice:** create a simple workflow (`.github/workflows/ci.yml`) that builds a Spring Boot project with Gradle (using the Gradle Wrapper, `./gradlew`), runs the tests, and publishes the test report as an artifact.

**Deliverable:** a pipeline that builds and tests on every PR, with working dependency caching (faster build on the second run).

**Prompt for Claude Code:**
> "Create a GitHub Actions workflow at `.github/workflows/ci.yml` triggered on push and pull_request, that builds a Spring Boot project using the Gradle Wrapper (`./gradlew build`), runs `./gradlew test`, publishes the test report as an artifact, and uses `actions/cache` to cache the Gradle dependency cache (`~/.gradle/caches`) and wrapper."

---

## Day 2 — Reusable Workflows and Templates

**Objectives:** `workflow_call`, composite actions, inputs/outputs/secrets between workflows, organization-level workflow templates (`.github/workflow-templates`).

**Practice:** refactor Day 1's pipeline into a **reusable workflow** parameterized by Java version and Gradle task/command, so it can be reused across multiple microservices without duplicating YAML.

**Deliverable:** `build-and-test.yml` reusable workflow + a minimal "caller" workflow that invokes it via `uses:` and passes inputs.

**Prompt for Claude Code:**
> "Refactor the CI workflow from the previous day into a reusable workflow (`workflow_call`) with inputs for `java-version` and `gradle-task` (defaulting to `build`). Also create an example caller workflow that invokes it."

---

## Day 3 — Building and Publishing Images to ECR

**Objectives:** multi-stage Dockerfile for a Gradle-built Spring Boot app, `docker/build-push-action`, Buildx, image versioning (commit SHA + semantic tag), authenticating to AWS **without static credentials** via OIDC (`aws-actions/configure-aws-credentials`).

**Practice:** a CI pipeline that builds, tests, builds the Docker image (via `./gradlew bootBuildImage` or a multi-stage Dockerfile using the Gradle build output), and publishes it to an ECR repository.

**Deliverable:** an image visible in ECR tagged `sha-<commit>` and `latest`/`v*` depending on the branch.

**Prompt for Claude Code:**
> "Add a job to the workflow that builds the Docker image for the Gradle-based Spring Boot app using Buildx (or `./gradlew bootBuildImage` if using Cloud Native Buildpacks), tags it with the commit SHA, and pushes it to Amazon ECR using `aws-actions/configure-aws-credentials` with OIDC (no static access keys)."

---

## Day 4 — OIDC Authentication + Integration with Your Existing Terraform

**Objectives:** how OIDC federation between GitHub and AWS works (identity provider, trust policy conditioned on `sub` for repo/branch), a least-privilege IAM role for pushing to ECR and deploying to EKS.

**Practice:** review/extend the Terraform project at `C:\dev\learning\terraform` to provision: an ECR repository, a GitHub OIDC provider (`aws_iam_openid_connect_provider`), and the IAM role the workflow will assume (with `aws-auth` in EKS mapping the role to Kubernetes permissions).

**Deliverable:** successful `terraform apply`; the IAM role exists and Day 3's workflow authenticates against it end-to-end.

**Prompt for Claude Code:**
> "Open the Terraform project at `C:\dev\learning\terraform`, review what AWS resources it already defines, and add (or adjust) the resources needed for: an ECR repository, a GitHub Actions OIDC provider, and an IAM role with a trust policy scoped to my repo/branch, with permissions to push to ECR and describe the EKS cluster."

---

## Day 5 — CD to EKS

**Objectives:** Kubernetes manifests for the Spring Boot app (Deployment, Service, ConfigMap/Secret, optionally Ingress), updating the image via `kubectl set image` or Kustomize, `aws eks update-kubeconfig` using the OIDC role.

**Practice:** a CD job that, after building/pushing to ECR, updates the image tag on the EKS cluster and verifies the rollout (`kubectl rollout status`).

**Deliverable:** a complete CI+CD pipeline: push to `main` → build → test → image to ECR → deploy to EKS → rollout verification.

**Prompt for Claude Code:**
> "Create Kubernetes manifests (Deployment, Service, ConfigMap) for the Spring Boot app under `k8s/`, using Kustomize to vary the image tag per environment. Add a CD job to the workflow that runs `aws eks update-kubeconfig`, applies the manifests, and waits for the rollout to complete."

---

## Day 6 — Best Practices and Hardening

**Objectives:** GitHub Environments with manual approvals for production, branch protection, secrets management (Environment secrets vs. repository secrets), security scanning (Trivy for the image, Dependabot/OWASP dependency-check, optionally CodeQL), notifications, automated semantic versioning.

**Practice:** add an approval gate before production deployment and a vulnerability scanning step that blocks the pipeline if critical CVEs are found in the image.

**Deliverable:** a pipeline with `dev`/`staging`/`prod` environments, manual approval on `prod`, and a Trivy scan acting as a quality gate.

**Prompt for Claude Code:**
> "Configure GitHub Environments (dev, staging, prod) with protection rules: manual approval required for prod. Add a Trivy scan step on the built image that fails the pipeline if critical or high vulnerabilities are found."

---

## Day 7 — Capstone Project

**Objective:** apply everything above to a realistic case with **two Spring Boot microservices** that share the reusable workflow, each with its own EKS deployment, and per-environment (dev/staging/prod) configuration managed consistently.

**Practice:**
1. Duplicate a second microservice and verify it reuses the Day 2 workflow without duplicating logic.
2. Both services are deployed to EKS with their own Kustomize manifests per environment.
3. Document the pipeline (repo README or wiki): flow diagram, how to rotate the OIDC role, how to add a third microservice.

**Final deliverable:** a repository (or repositories) with a reproducible, documented, end-to-end CI/CD pipeline that's reusable for new microservices.

**Prompt for Claude Code:**
> "Take the reusable pipeline already built and apply it to a second Spring Boot microservice. Generate a README documenting the full CI/CD flow, how environments are managed, and how to add a new microservice to the pattern."

---

## Quick Notes

- If the 1-week pace feels tight on any given day, Days 6 and 7 are the easiest to merge or extend by a day or two — the technical core (CI, ECR, EKS) is already covered by Day 5.
- Each "Prompt for Claude Code" is a starting point; it's best to iterate with Claude Code directly in your real repo, not in the abstract.
- Keep the GitHub OIDC IAM role scoped to least privilege (ECR push + EKS describe/deploy) from Day 4 onward — don't widen it "just in case."
