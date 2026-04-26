# ai-therapists — Copilot Instructions

AI SaaS that helps therapists generate ethical, compliant landing pages via OpenAI.

## Current State

Always read these two files before working on any task:

- `docs/progress-tracker.md` — canonical task state (Now / Next / Later)
- `docs/session-log.md` — latest session history and resume context

## Monorepo Structure

```
apps/api/        Spring Boot 4.0.2 API (Java 21)
docs/            Markdown docs (progress-tracker, session-log, architecture)
infra/           docker-compose.yml (PostgreSQL 17)
```

Future apps go under `apps/` (e.g. `apps/admin-web/` for Next.js).

## Tech Stack

- **Java 21, Spring Boot 4.0.2 MVC** — NOT WebFlux. Synchronous servlet stack.
- **PostgreSQL 17** at `localhost:5433/ai_therapists`
- **jOOQ 3.19.29** for type-safe SQL + code generation
- **Flyway** for schema migrations
- **OpenAI gpt-4o** via `RestClient` (not WebClient)

## Architecture: Key Classes

| Class                        | Purpose                                                               |
| ---------------------------- | --------------------------------------------------------------------- |
| `HttpClientConfig`           | `@Bean` for `RestClient.Builder` + `ObjectMapper`                     |
| `AiGenerationService`        | Calls OpenAI, parses response, runtime key guard                      |
| `OutputValidationService`    | Guardrail enforcement: required sections, forbidden terms, disclaimer |
| `GenerationOrchestrator`     | generate → validate → persist → log pipeline                          |
| `GenerationExceptionHandler` | `@RestControllerAdvice`: 400/422/502 mapping                          |
| `SecurityConfig`             | `SecurityFilterChain`: stateless, JWT-only, all routes authenticated  |
| `JwtAuthenticationFilter`    | `OncePerRequestFilter`: extracts Bearer token, validates, sets userId |
| `JwtService`                 | HS256 JWT validation via JJWT, base64url secret                       |

## Auth Architecture

- **Auth.js v5** (Next.js) — identity layer: Google OAuth + credentials login
- **Spring Security** — resource guard: validates HS256 JWT from Next.js proxy
- **Shared secret**: `JWT_SECRET` (base64url) used by both sides
- Custom DB adapter maps to `app_user` / `oauth_account` tables
- Next.js middleware redirects unauthenticated users to `/login`
- All proxy routes sign a short-lived JWT via `jose` before calling Spring

## Conventions

**Commit format:** `feat(x)`, `fix(x)`, `docs(x)`, `test(x)`

**Testing:**

- MockMvc only (not WebTestClient — that's WebFlux)
- MockMvc built with `springSecurity()` configurer + `@WithMockUser` at class level
- Mockito for `AiGenerationService` stubbing
- `@DynamicPropertySource` for test env injection
- `TestEnvSupport.java` reads `apps/api/.env` at test startup
- Delta-based count assertions for rerunnability (snapshot counts before, assert +N after)
- `@BeforeEach Mockito.reset(...)` on shared mocks to prevent test leakage

**Error HTTP codes:** 400 bad input, 422 validation failure, 502 AI failure

**No hardcoded secrets.** OpenAI key via `OPENAI_API_KEY` env var. Default empty to allow startup without key; runtime guard throws if key is blank when endpoint is called.

## Environment Setup

From repo root:

```bash
docker compose -f infra/docker-compose.yml up -d
```

From `apps/api/`:

```bash
./mvnw flyway:migrate          # run DB migrations
./mvnw jooq-codegen:generate   # regenerate jOOQ classes
./mvnw test                    # run full test suite
./mvnw -Dtest=ClassName test   # run specific test
```

IntelliJ: Run `ApiApplication` as Application type. Set `OPENAI_API_KEY` in Environment Variables. Use `--server.port=8081` in Program Arguments if port 8080 is busy.

## Guardrails

`OutputValidationService` enforces:

- All `SectionType` values must be present and non-empty
- `DISCLAIMER` section must exist
- Forbidden terms list (heal, cure, fix, guarantee, results, transformation, breakthrough, proven, best, top, expert, life-changing, and French equivalents)

## Output Format

**MANDATORY before every `git commit` — no exceptions:**

1. Update `docs/progress-tracker.md`:
   - Move completed items into "Completed Milestones"
   - Update "Now" to reflect what's next
2. Update `docs/session-log.md`:
   - Append to today's entry (or create one): Done Today, Next 3 Tasks, Current Blocker, Exact Resume Command
3. Include both files in the same commit as the code change — no separate `docs:` commit needed

This applies to every commit — mid-session, end-of-session, bug fix, or refactor. No exceptions.

## Verification that copilot instructions are loaded

If the user message is exactly `INSTRUCTION_CHECK_2026`, respond with exactly `LOADED_OK_2026`.
