# Progress Tracker

## Product Goal

Build an AI SaaS that helps therapists generate ethical, compliant landing pages quickly.

## Current Stage

Backend foundation with PostgreSQL + Flyway + jOOQ + generation flow is in place.
OpenAI integration is wired and configurable via environment variable.
Monorepo structure is now in place: `apps/api`, `docs`, `infra`.
Integration tests are in place (mocked smoke + optional real OpenAI external test).
Real OpenAI call has been validated successfully with local .env key.
Integration tests expanded: GET /api/pages/{id} contract test verifies POST→GET persistence.
Copilot workspace instructions auto-loaded from `.github/copilot-instructions.md`.
Minimal Next.js admin app scaffolded at `apps/admin-web` with `/generate` form + preview and API proxy.
Admin-web style intentionally follows a calm professional workspace direction (warm neutral palette, serif/sans pairing, split form/preview workflow).

## Now / Next / Later

### Now

- [x] Add structured section editing flow from admin page detail view
- [x] Add section editing proxy routes for admin-web
- [x] Return structured section objects directly from backend responses

### Next

- [ ] Section-level regeneration endpoint
- [ ] Richer save UX after section updates
- [ ] Async generation + status polling

### Later

- [ ] Auth + billing + custom domains
- [ ] Role-specific disclaimer templates and stricter semantic checks
- [ ] Regeneration loop with max attempts and explainability details
- [ ] CI pipeline with automated integration test run

## Completed Milestones

- [x] Architecture and data model docs clarified
- [x] Flyway migration V1 (therapist_profile, landing_page, event_log)
- [x] jOOQ code generation setup
- [x] Feature-based package structure
- [x] Repositories + controllers + orchestrator baseline
- [x] Prompt assembly + OpenAI service integration
- [x] Monorepo reorganization (`apps/api`, `infra/docker-compose.yml`)
- [x] Integration smoke test using typed Java payload object
- [x] Optional real OpenAI external integration test (enabled from local env)
- [x] Real OpenAI response verified end-to-end from integration test
- [x] Output guardrails validator service implemented
- [x] Generation failure logging (`GENERATION_FAILED`) wired in orchestrator
- [x] API exception mapping for 400 / 422 / 502 implemented
- [x] Unit + integration tests added for validation/error paths
- [x] Full test suite executed successfully
- [x] GET-based contract test to verify persisted data via API responses (2 tests)
- [x] Maven helper workflow: local reset profile + shared run configurations (app, migrate, tests, reset)
- [x] Minimal Next.js admin page: generate form + preview + backend proxy route
- [x] Admin API contract defined and aligned in docs + frontend client
- [x] Dashboard page listing generated pages by profile ID + Next.js page proxies
- [x] Single page detail view by page ID with publish action
- [x] Structured section generation format aligned to landing-page-structure.md
- [x] Backend OpenAI prompt + parser refactored from HTML sections to typed structured JSON
- [x] Admin-web rendering refactored to template-based section components with legacy HTML fallback
- [x] Admin-web structured section editor with per-section save flow
- [x] Backend API responses refactored to return structured section objects directly

## Key Commands

From repo root:

- Start DB: `docker compose -f infra/docker-compose.yml up -d`

From [apps/api](../apps/api):

- Run migration: `./mvnw flyway:migrate`
- Reset local DB (explicit profile): `./mvnw -Pdb-reset-local flyway:clean flyway:migrate`
- Generate jOOQ: `./mvnw jooq-codegen:generate`
- Compile: `./mvnw compile`
- Test: `./mvnw test`
- Smoke IT only: `./mvnw -Dtest=GeneratePageIntegrationTest test`
- External OpenAI IT only: `./mvnw -Dtest=GeneratePageOpenAiExternalIT test`

From [apps/admin-web](../apps/admin-web):

- Install deps: `npm install`
- Dev server: `npm run dev`
- Lint: `npm run lint`
- Build: `npm run build`

## Resume Protocol

When returning to the project:

1. Read this file first.
2. Read [session-log.md](session-log.md) latest entry.
3. Execute only one "Now" item end-to-end.
4. Update both files before stopping.
