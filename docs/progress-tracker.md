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

- [ ] Add `BLOB_READ_WRITE_TOKEN` to `.env.local` + browser-test full flow incl. photo upload
- [ ] Wire ethorai.fr / ethorai.com to Vercel
- [ ] 5 user interviews with target therapists (validate generated page output before further product changes)

## Completed Milestones

- [x] Photo upload: Flyway V6 + Vercel Blob upload route + PhotoScreen (step 9 in onboarding, optional) + HeroSection side-by-side layout with circular photo; `next/image` + remotePatterns; all tests green
- [x] Fix publish 502: `revalidatePath` was inside the proxy try/catch; split fetch and revalidation into separate error paths
- [x] Multi-contact onboarding: replaced single contactMethod/contactValue with phone + email + bookingLink (V5 migration, full-stack); ContactScreen multi-select toggle cards + French phone auto-formatter; ContactSection CTA now prefers booking link; 28/28 tests green
- [x] Enriched `AREAS_OF_SUPPORT` and `WHAT_YOU_CAN_EXPECT` from `List<String>` → `List<{title, description}>` end-to-end (data records, prompt schema + rules, mapper with backward-compat parser, test fixture, frontend types + renderer + editor); sticky header on the public page
- [x] Public page redesign (Phase 3) — rewrote all 9 section components to match Ethorai brand (warm cream + Fraunces serif + stone palette); replaced blue/gray Bootstrap-era styling; functional CTA with mailto/tel fallback; added `--font-serif` Tailwind theme variable
- [x] Workspace + cleanup (Phase 1+2) — singleton `/page` workspace replaces `/pages/[id]` (no ID in URL); deleted `/dashboard`, `/generate`, `/pages/[id]`, `/api/pages?profileId=`; new backend `MeController` with `GET /api/me/page` and `GET /api/me/profile` (204 when empty); root `/` redirects to `/page`; "Modifier mes réponses" pre-fills onboarding from profile; "Régénérer" / "Publier" / "Voir public" / "Copier le lien" / "Se déconnecter" actions in sticky top bar; toast on publish-with-clipboard-copy
- [x] Conversational onboarding flow at `/onboarding` — 10 screens (welcome + 8 questions + summary), Typeform-style one-question-per-screen, localStorage persistence, French copy, Tailwind only; root `/` now redirects to `/onboarding` instead of `/dashboard`
- [x] Rate limiting on POST /api/generate (per-user, prevents OpenAI cost abuse)
- [x] Sanitize free-text inputs against prompt injection (approach field)
- [x] XSS audit on section-renderers.tsx — all fields use JSX interpolation, no dangerouslySetInnerHTML, confirmed safe
- [x] GitHub Actions CI pipeline (`./mvnw test` + `npm run build` on push to main)
- [x] Public page route: `/p/[id]` ISR Server Component fetching from `GET /api/public/pages/{id}` (no auth, PUBLISHED only), `revalidate=3600`
- [x] On publish: `revalidatePath('/p/{id}')` called in Next.js publish proxy route after Spring 204
- [x] Dockerfile (multi-stage, eclipse-temurin:21-jdk builder + 21-jre runtime) + `.dockerignore` — Docker build verified locally
- [x] jOOQ generated sources committed to `src/main/generated/jooq` — builds without a DB (`-Djooq.codegen.skip=true`)
- [x] Project named **Ethorai** — GitHub org `ethorai` created, monorepo pushed to `ethorai/ethorai-monorepo`
- [x] Railway deployment: Spring API + PostgreSQL 17 managed instance; fixed `server.port` root placement + PG individual vars (`PGHOST/PGPORT/PGDATABASE/PGUSER/PGPASSWORD`)
- [x] Vercel deployment: Next.js admin + public pages; fixed `middleware.ts` → `proxy.ts` (Next.js 16), `AUTH_SECRET`, Google OAuth `client_id=undefined` (explicit credentials in `Google({clientId, clientSecret})`), missing redirect URI in Google Cloud Console
- [x] `DotEnvEnvironmentPostProcessor` — loads `apps/api/.env` at Spring startup via SPI; fixes 401 on Google login caused by `INTERNAL_SECRET` missing from local environment
- [x] Generation retry logic — `generateWithRetry()` in orchestrator retries up to 3 times on `GenerationValidationException` (GPT-4o forbidden term slippage); surface error only if all 3 attempts fail

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
- [x] GitHub Actions CI: `.github/workflows/ci.yml` — Spring tests (PostgreSQL 17 service) + Next.js lint + build, runs on push/PR to main; updated to skip jOOQ codegen (-Djooq.codegen.skip=true)
- [x] Dockerfile multi-stage build (eclipse-temurin:21-jdk → 21-jre); committed jOOQ sources to src/main/generated/jooq; pom.xml: skip flag + build-helper-maven-plugin for source registration
- [x] Auth refactor: Spring owns all user management — POST /api/auth/login (issues JWT) + POST /api/auth/oauth (upserts Google user, issues JWT, protected by INTERNAL_SECRET); Next.js calls Spring for both flows; no direct DB access from Next.js; DATABASE_URL removed from Vercel requirements
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
