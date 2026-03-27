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

## Now / Next / Later

### Now

- [ ] Define admin API contract for Next.js form submission + preview payload
- [ ] Add dashboard page listing generated pages by profile ID
- [ ] Add single page detail view by page ID in admin app

### Next

- [ ] Section-level regeneration endpoint
- [ ] Async generation + status polling
- [ ] Auth + billing + custom domains

### Later

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
