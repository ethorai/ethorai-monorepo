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
Authentication layer: Spring Security JWT filter + Auth.js v5 (Google OAuth + credentials) with shared HS256 JWT secret pattern.

## Now / Next / Later

### Now

- [x] Rate limiting on POST /api/generate (per-user, prevents OpenAI cost abuse)
- [x] Sanitize free-text inputs against prompt injection (approach field)
- [x] XSS audit on section-renderers.tsx — all fields use JSX interpolation, no dangerouslySetInnerHTML, confirmed safe
- [x] GitHub Actions CI pipeline (`./mvnw test` + `npm run build` on push to main)
- [x] Public page route: `/p/[id]` ISR Server Component fetching from `GET /api/public/pages/{id}` (no auth, PUBLISHED only), `revalidate=3600`
- [x] On publish: `revalidatePath('/p/{id}')` called in Next.js publish proxy route after Spring 204
- [ ] Dockerfile for Spring Boot API + `.dockerignore` (reusable for Railway, ECS, any container host)
- [ ] Deploy to Railway (Spring API + PostgreSQL managed) + Vercel (admin-web + public pages)
- [ ] Wire "View public page" link on admin page detail (`/pages/[id]`) pointing to `/p/{id}`

## Completed Milestones

- [x] Full project review (architecture, docs, security gaps, monetization options)
- [x] docs/persona.md — removed ChatGPT transcript contamination
- [x] docs/architecture.md — corrected stale decisions (auth, async gen, ISR deploy, section regen)
- [x] docs/data-model.md — replaced JPA @Entity annotations with jOOQ records, updated StructuredSections
- [x] docs/prompt-template.md — replaced text-based output format with actual JSON schema
- [x] README.md — created with architecture, tech decisions, local setup, env vars reference
- [x] CLAUDE.md — created with project context, conventions, and session protocol for Claude Code

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
- [x] Section-level regeneration endpoint with per-section OpenAI prompt
- [x] Richer save UX: per-section feedback, dirty tracking, auto-dismiss, beforeunload warning
- [x] Async generation with job queue, status polling, and spinner UI
- [x] Prettier + ESLint VS Code workspace config
- [x] Spring Security: JWT filter chain (SecurityConfig, JwtService, JwtAuthenticationFilter)
- [x] Flyway V3 auth schema (app_user, oauth_account, verification_token) + V4 landing_page.user_id
- [x] Auth.js v5: Google OAuth + Credentials providers, custom DB adapter, JWT session strategy
- [x] Login page, middleware route guard, JWT forwarding to Spring from all proxy routes
- [x] userId scoping: SecurityContextHelper, LandingPageRepository row-level userId filter, GenerationOrchestrator + controllers updated, test user fixture added (12/12 tests green)
- [x] Credentials sign-up: AuthController POST /api/auth/register (public), BCrypt hashing, 409 on duplicate email; /register Next.js page with auto sign-in; middleware updated
- [x] Middleware auth guard fix: edge-safe auth.config.ts split, middleware moved to src/, root / redirects to /dashboard
- [x] Per-user rate limiting on POST /api/generate (Bucket4j, 5 req/hour, returns 429) + unit + integration tests
- [x] Prompt injection sanitization on InputNormalizationService (IGNORE/DISREGARD/OVERRIDE patterns + role markers + length limits) + 7 unit tests
- [x] XSS audit: section-renderers.tsx uses pure JSX interpolation, no dangerouslySetInnerHTML — no fix needed
- [x] GitHub Actions CI: `.github/workflows/ci.yml` — Spring tests (PostgreSQL 17 service) + Next.js lint + build, runs on push/PR to main
- [x] Public page: `PublicPageController` + `findByIdPublic()` (no userId, PUBLISHED only) + `/api/public/**` permitted in SecurityConfig
- [x] Next.js `/p/[id]` ISR page with `generateMetadata`, `revalidate=3600`, renders all 9 section components
- [x] On-demand revalidation: publish proxy calls `revalidatePath('/p/{id}')` after successful Spring publish
- [x] Fixed login page `useSearchParams` Suspense boundary (Next.js 16 build requirement)

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
