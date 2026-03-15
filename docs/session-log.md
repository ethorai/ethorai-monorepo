# Session Log

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
