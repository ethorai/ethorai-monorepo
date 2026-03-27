# Session Log

## 2026-03-27

### Done Today

- Implemented output validation layer:
  - [OutputValidationService](../apps/api/src/main/java/com/ai/therapists/api/generation/OutputValidationService.java)
  - [GenerationValidationException](../apps/api/src/main/java/com/ai/therapists/api/generation/GenerationValidationException.java)
- Wired validation into [GenerationOrchestrator](../apps/api/src/main/java/com/ai/therapists/api/generation/GenerationOrchestrator.java) before persistence.
- Added failure logging on generation errors (`GENERATION_FAILED`) with payload in event log.
- Added centralized HTTP error handling:
  - [GenerationExceptionHandler](../apps/api/src/main/java/com/ai/therapists/api/generation/GenerationExceptionHandler.java)
  - [GenerationErrorResponse](../apps/api/src/main/java/com/ai/therapists/api/generation/GenerationErrorResponse.java)
- Added tests:
  - [OutputValidationServiceTest](../apps/api/src/test/java/com/ai/therapists/api/generation/OutputValidationServiceTest.java)
  - [GenerationErrorIntegrationTest](../apps/api/src/test/java/com/ai/therapists/api/GenerationErrorIntegrationTest.java)
- Verified all tests pass with full suite run (`./mvnw test`, exit code 0).
- Implemented GET-based contract integration test:
  - [GetPageIntegrationTest](../apps/api/src/test/java/com/ai/therapists/api/GetPageIntegrationTest.java)
  - Verifies POST→GET persistence: create via `/api/generate`, verify via `GET /api/pages/{id}`
  - Tests both successful read and 404 not-found scenario
  - Full test suite now 11 tests, all passing (exit code 0)
- Added `.github/copilot-instructions.md` for auto-loading workspace context across sessions/machines
- Discussed context window management: repo memory for AI, committed docs for humans, test boundaries
- Added Maven safety/profile workflow in [apps/api/pom.xml](../apps/api/pom.xml):
  - `flyway.cleanDisabled=true` by default
  - `db-reset-local` profile toggles `flyway.cleanDisabled=false` for explicit local reset
- Added shared IntelliJ run configurations in project `.run/` for dropdown execution:
  - `API - Run App`
  - `API - Run All Tests`
  - `API - Flyway Migrate`
  - `API - Flyway Reset Local`
- Verified commands from terminal:
  - `./mvnw flyway:migrate` → exit 0
  - `./mvnw test` → exit 0
  - `./mvnw -Pdb-reset-local flyway:clean flyway:migrate` → exit 0
- Scaffolded frontend app at [apps/admin-web](../apps/admin-web) (Next.js 16 + TypeScript).
- Implemented admin generation flow:
  - [src/app/generate/page.tsx](../apps/admin-web/src/app/generate/page.tsx) form with preview panel
  - [src/lib/api.ts](../apps/admin-web/src/lib/api.ts) typed client for generate request/response
  - [src/app/api/generate/route.ts](../apps/admin-web/src/app/api/generate/route.ts) proxy route to backend (`API_BASE_URL`, default `http://localhost:8080`)
- Replaced boilerplate home page with entry point to generate workspace:
  - [src/app/page.tsx](../apps/admin-web/src/app/page.tsx)
- Applied custom typography/theme and metadata:
  - [src/app/layout.tsx](../apps/admin-web/src/app/layout.tsx)
  - [src/app/globals.css](../apps/admin-web/src/app/globals.css)
  - [next.config.ts](../apps/admin-web/next.config.ts) with `turbopack.root` to silence monorepo lockfile warning
- Verified frontend checks:
  - `npm run lint` → exit 0
  - `npm run build` → exit 0

### Next 3 Tasks

1. Define admin API contract for form submission + preview payload.
2. Add dashboard page listing generated pages by profile ID.
3. Add single page detail view by page ID in admin app.

### Now (updated)

1. ~~GET-based contract integration test~~ DONE
2. ~~Maven helper commands/profiles~~ DONE
3. ~~Build minimal Next.js admin page: generate form + preview~~ DONE

### Current Blocker

No blocker.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From [apps/api](../apps/api):
`./mvnw test`

---

## 2026-03-15 (Final checkpoint before first commit)

### Done Today

- Verified real OpenAI integration using local `.env` key and paid credits.
- Confirmed external test returns complete structured sections and compliant tone.
- Kept architecture choice on Spring MVC (no WebFlux migration for MVP).
- Confirmed testing strategy split:
  - mocked smoke integration test for stability
  - optional external OpenAI integration test for real endpoint verification

### Next 3 Tasks

1. Implement output guardrails validator service (forbidden words + required sections + disclaimer).
2. Wire validation failures into orchestrator + event log (`GENERATION_FAILED`).
3. Add API error mapping for generation/validation failures.

### Current Blocker

No blocker.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From [apps/api](../apps/api):
`./mvnw -Dtest=GeneratePageIntegrationTest test`

Optional real OpenAI verification:
`./mvnw -Dtest=GeneratePageOpenAiExternalIT test`

---

## 2026-03-15 (Checkpoint before first commit)

### Done Today

- Confirmed architecture direction: keep Spring MVC (no WebFlux migration now).
- Reorganized repository as monorepo:
  - [apps/api](../apps/api)
  - [docs](../docs)
  - [infra/docker-compose.yml](../infra/docker-compose.yml)
- Fixed runtime wiring issues for `AiGenerationService` dependencies (`RestClient.Builder`, `ObjectMapper`).
- Added integration testing strategy:
  - [GeneratePageIntegrationTest](../apps/api/src/test/java/com/ai/therapists/api/GeneratePageIntegrationTest.java) for smoke flow
  - [GeneratePageOpenAiExternalIT](../apps/api/src/test/java/com/ai/therapists/api/GeneratePageOpenAiExternalIT.java) for real OpenAI opt-in
- Refactored test payloads to typed Java objects (`TherapistInput`) instead of raw JSON strings.

### Next 3 Tasks

1. Implement output guardrails validator service (forbidden words + required sections + disclaimer).
2. Wire validation failures into orchestrator + event log (`GENERATION_FAILED`).
3. Add API error mapping (clear HTTP response when AI generation/validation fails).

### Current Blocker

No blocker.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From [apps/api](../apps/api):
`./mvnw -Dtest=GeneratePageIntegrationTest test`

Optional real OpenAI test:
`./mvnw -Dtest=GeneratePageOpenAiExternalIT test`

---

## 2026-03-15

### Done Today

- Built backend baseline with Flyway, jOOQ, repositories, controllers, and orchestrator.
- Restructured codebase to feature-based packages.
- Added OpenAI integration components:
  - `PromptAssemblyService`
  - `AiGenerationService`
  - `AiGenerationException`
- Added OpenAI config in [application.yaml](../apps/api/src/main/resources/application.yaml).

### Next 3 Tasks

1. Verify OpenAI generation path with real key and real request. (Done)
2. Implement guardrail output validator (forbidden words, mandatory sections, disclaimer).
3. Wire guardrail validator into orchestrator with clear failure event logging.

### Current Blocker

No blocker.

### Exact Resume Command

From repo root, start DB first:
`docker compose -f infra/docker-compose.yml up -d`

Then from [apps/api](../apps/api):
`OPENAI_API_KEY=your_key_here ./mvnw spring-boot:run`

---

## Template

### YYYY-MM-DD

### Done Today

-

### Next 3 Tasks

1.
2.
3.

### Current Blocker

-

### Exact Resume Command

-
