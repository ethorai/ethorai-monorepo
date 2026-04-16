# ai-therapists — Claude Instructions

AI SaaS that helps solo EU therapists generate ethical, compliant landing pages via OpenAI.  
Spring Boot 4.0.2 (Java 21) backend + Next.js 15 admin + Auth.js v5 auth layer.

---

## Before Starting Any Task

Always read these two files first:

- `docs/progress-tracker.md` — canonical task state (Now / Next / Later + completed milestones)
- `docs/session-log.md` — latest session history, blockers, and exact resume commands

Do not start working until you have read both.

---

## Monorepo Structure

```
apps/api/          Spring Boot 4.0.2 API (Java 21)
apps/admin-web/    Next.js 15 admin platform + public pages
docs/              Architecture, data model, guardrails, generation flow
infra/             docker-compose.yml (PostgreSQL 17 on port 5433)
```

---

## Tech Stack — Critical Details

- **Java 21, Spring Boot 4.0.2 MVC** — NOT WebFlux. Synchronous servlet stack only.
- **PostgreSQL 17** at `localhost:5433/ai_therapists`
- **jOOQ 3.19.29** for all DB queries — no JPA, no Hibernate, no `@Entity`
- **Flyway** for schema migrations in `apps/api/src/main/resources/db/migration/`
- **OpenAI gpt-4o** via `RestClient` (not `WebClient`)
- **Next.js 15** with App Router, TypeScript, Auth.js v5
- **Turbopack** enabled for local dev

---

## Key Backend Classes

| Class | Package | Purpose |
|---|---|---|
| `GenerationOrchestrator` | `generation` | Coordinates the full pipeline: normalize → prompt → AI → validate → persist → log |
| `PromptAssemblyService` | `generation` | Builds the system + user prompt from inputs, persona, guardrails, role policy |
| `AiGenerationService` | `generation` | Calls OpenAI via `RestClient`, parses JSON response into `StructuredSections` |
| `OutputValidationService` | `generation` | Enforces guardrails: required sections, forbidden terms, disclaimer presence |
| `GenerationExceptionHandler` | `generation` | `@RestControllerAdvice`: maps exceptions to 400/422/502 |
| `SecurityConfig` | `security` | Stateless JWT filter chain — all routes authenticated except `/api/auth/register` |
| `JwtAuthenticationFilter` | `security` | `OncePerRequestFilter`: extracts Bearer token, validates, sets userId in SecurityContext |
| `JwtService` | `security` | HS256 JWT validation via JJWT, base64url secret |
| `SecurityContextHelper` | `security` | Extracts `UUID userId` from the current SecurityContext |
| `StructuredSections` | `section_data` | Record aggregating all 9 typed section objects (HeroData, ContactData, …) |

---

## Auth Architecture

- **Auth.js v5** (Next.js) — identity layer: Google OAuth + email/password credentials
- **Spring Security** — resource guard: validates HS256 JWT on every request
- **Shared secret**: `JWT_SECRET` (base64url) — must match on both sides
- Next.js proxy routes sign a short-lived JWT via `jose` before calling Spring (`spring-fetch.ts`)
- All page endpoints are scoped by `userId` — row-level filtering in every repository method
- Public endpoint: `POST /api/auth/register` (no JWT required)

---

## Testing Conventions

- **MockMvc only** — never `WebTestClient` (that's WebFlux)
- MockMvc built with `.apply(springSecurity())` configurer
- `@WithMockUser(username = "00000000-0000-0000-0000-000000000001")` at class level on all test classes
- `@BeforeEach` inserts test user via jOOQ upsert (`ON CONFLICT DO NOTHING`)
- Mockito for `AiGenerationService` stubbing — never make real OpenAI calls in standard tests
- Real OpenAI tests live in `GeneratePageOpenAiExternalIT` — opt-in only via `-Dtest=...`
- `TestEnvSupport.java` reads `apps/api/.env` at test startup for local secrets
- **Delta-based count assertions**: snapshot DB counts before, assert `+N` after — tests are rerunnnable
- `@BeforeEach Mockito.reset(...)` on shared mocks to prevent test leakage
- HTTP error codes: `400` bad input, `422` guardrail/validation failure, `502` AI call failure

---

## Guardrails

`OutputValidationService` enforces at generation time:

- All 9 `SectionType` values must be present and non-empty
- `DISCLAIMER` section is mandatory — missing disclaimer fails generation
- Forbidden vocabulary list (English + French): `heal`, `cure`, `fix`, `guarantee`, `results`, `transformation`, `breakthrough`, `proven`, `best`, `top`, `expert`, `life-changing`, `résultats`, and equivalents
- Role-based strictness: Psychologist > Therapist > Counselor

See `docs/ai-guardrails.md` for the full constraint specification.

---

## Commit Format

```
feat(scope): description
fix(scope): description
docs(scope): description
test(scope): description
refactor(scope): description
```

---

## Environment Variables

**`apps/api`:**
- `OPENAI_API_KEY` — blank by default, runtime guard throws if blank when endpoint is called
- `JWT_SECRET` — base64url HS256 secret, must match admin-web

**`apps/admin-web`:**
- `API_BASE_URL` — Spring Boot base URL
- `AUTH_SECRET` — Auth.js session secret
- `JWT_SECRET` — must match Spring backend
- `DATABASE_URL` — PostgreSQL URL for Auth.js DB adapter
- `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` — optional, needed for Google OAuth

---

## Key Commands

From repo root:
```bash
docker compose -f infra/docker-compose.yml up -d   # start PostgreSQL
```

From `apps/api/`:
```bash
./mvnw flyway:migrate                              # run migrations
./mvnw jooq-codegen:generate                       # regenerate jOOQ classes after schema change
./mvnw test                                        # full test suite
./mvnw -Dtest=GeneratePageOpenAiExternalIT test    # real OpenAI test (needs OPENAI_API_KEY)
./mvnw -Pdb-reset-local flyway:clean flyway:migrate # wipe and remigrate local DB
```

From `apps/admin-web/`:
```bash
npm run dev    # dev server (Turbopack)
npm run lint   # ESLint
npm run build  # production build check
```

---

## End-of-Session Protocol — Mandatory

At the end of every working session, before stopping:

1. **Update `docs/progress-tracker.md`**:
   - Move completed items to "Completed Milestones"
   - Set "Now" to the next actionable items

2. **Update `docs/session-log.md`**:
   - Add a new dated entry with: Done Today, Next 3 Tasks, Current Blocker, Exact Resume Command

3. **Commit both files together**:
   ```
   docs: update session log + progress tracker
   ```

Skipping either file is a mistake. Both files, always, before ending a session.
