# AI Therapists — Landing Page Generator

An AI-powered SaaS that helps solo therapists in the EU create professional, ethical, and compliant landing pages through a guided form. No marketing jargon, no outcome promises — just clear, responsible copy.

Built as both a real product and a portfolio project demonstrating AI orchestration, guardrail-based generation, and responsible AI design in a regulated context.

---

## How It Works

A therapist fills a 7-question form. The system assembles a structured prompt with ethical guardrails and role-based policies, calls the OpenAI API, validates the output, and produces a 9-section landing page. If the output violates any constraint, the system regenerates automatically (up to 3 attempts).

```
Therapist fills form (Next.js)
        │
        ▼
POST /api/generate  →  202 Accepted + jobId
        │
        ▼
Background job:
  1. Normalize inputs
  2. Select role policy (Psychologist / Therapist / Counselor)
  3. Assemble prompt (persona + guardrails + role policy + inputs)
  4. Call OpenAI API → structured JSON output
  5. Validate (vocabulary, tone, claims, structure, disclaimer)
  6. Regenerate if violations detected (max 3 attempts)
  7. Persist to PostgreSQL
        │
        ▼
Frontend polls GET /api/generate/status/{jobId}
        │
        ▼
On completion → page visible at /p/{slug}
```

---

## Tech Stack

| Layer | Technology | Hosting |
|---|---|---|
| Backend API | Spring Boot 4.0.2, Java 21, jOOQ | Railway |
| Database | PostgreSQL + Flyway migrations | Railway |
| Admin platform | Next.js (React), Auth.js v5 | Vercel |
| Public pages | Next.js ISR (`/p/[slug]`) | Vercel |
| AI provider | OpenAI API (gpt-4o) | — |

---

## Key Design Decisions

**jOOQ over JPA/Hibernate** — Explicit, type-safe SQL with full control over JSONB operations. No lazy-loading surprises or ORM magic.

**Structured JSON output from AI** — The prompt instructs the model to return a single JSON object matching a fixed schema. The backend deserializes directly into typed Java records. No free-form HTML parsing.

**Guardrail-based generation** — Output is validated programmatically before being stored. Forbidden vocabulary, outcome-based claims, missing disclaimer, structural violations — any of these trigger regeneration. The guardrails are documented in [`docs/ai-guardrails.md`](docs/ai-guardrails.md).

**Role-aware policy enforcement** — Psychologists, therapists, and counselors operate under different ethical constraints. The role selected in the form determines which policy is applied at generation time.

**Async generation with job queue** — Generation involves an OpenAI call and a validation loop. The endpoint returns `202 Accepted` immediately with a job ID; the frontend polls for completion. Avoids HTTP timeout issues.

**Stateless JWT auth** — Spring Security validates a short-lived HS256 JWT on every request. Auth.js v5 (Next.js side) handles identity (Google OAuth + email/password), signs the JWT with a shared secret, and forwards it to Spring on every API proxy call.

**Next.js ISR for public pages** — Generated pages are stored in PostgreSQL. On publish, the backend triggers Next.js On-Demand Revalidation. Pages are CDN-cached and served from `/p/[slug]` without a per-request DB hit.

---

## Project Structure

```
ai-therapists/
├── apps/
│   ├── api/                          # Spring Boot backend
│   │   └── src/main/java/com/ai/therapists/api/
│   │       ├── auth/                 # Registration, credentials
│   │       ├── generation/           # Orchestrator, prompt assembly, OpenAI client
│   │       ├── page/                 # Page CRUD, publish, section update
│   │       ├── profile/              # TherapistProfile repository
│   │       ├── section_data/         # Typed section records (HeroData, ContactData, …)
│   │       ├── security/             # JWT filter, SecurityConfig
│   │       ├── event/                # Append-only event log
│   │       └── config/               # AI client config, jOOQ config
│   └── admin-web/                    # Next.js admin + public pages
│       └── src/
│           ├── app/
│           │   ├── dashboard/        # Lists therapist's pages
│           │   ├── generate/         # 7-question form + async preview
│           │   ├── pages/[id]/       # Section editor + publish action
│           │   ├── login/            # Auth.js login (Google + credentials)
│           │   └── register/         # Credentials sign-up
│           ├── components/           # Section renderers + editors
│           └── lib/
│               ├── api.ts            # Spring API client
│               ├── spring-fetch.ts   # withAuth() helper (JWT forwarding)
│               └── db-adapter.ts     # Auth.js custom DB adapter
├── docs/                             # Architecture, data model, guardrails, …
└── infra/
    └── docker-compose.yml            # Local PostgreSQL
```

---

## Local Setup

### Prerequisites

- Java 21
- Node.js 20+
- Docker (for local PostgreSQL)
- An OpenAI API key

### 1. Start the database

```bash
docker compose -f infra/docker-compose.yml up -d
```

### 2. Configure the backend

Create `apps/api/.env` (gitignored) or set these as environment variables:

```env
OPENAI_API_KEY=your_openai_api_key_here
JWT_SECRET=your_base64url_secret_here
```

The datasource defaults to `localhost:5433/ai_therapists` (see `application.yaml`). Override if needed via Spring properties.

### 3. Run migrations and start the API

```bash
cd apps/api
./mvnw flyway:migrate
./mvnw spring-boot:run
```

API available at `http://localhost:8080`.

### 4. Configure the admin app

Create `apps/admin-web/.env.local`:

```env
API_BASE_URL=http://localhost:8080
AUTH_SECRET=your_auth_secret_here         # any random 32-char string
JWT_SECRET=your_jwt_secret_here           # must match Spring's jwt.secret
GOOGLE_CLIENT_ID=your_google_client_id    # optional, needed for Google OAuth
GOOGLE_CLIENT_SECRET=your_google_secret   # optional
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/ai_therapists
```

### 5. Start the admin app

```bash
cd apps/admin-web
npm install
npm run dev
```

Admin available at `http://localhost:3000`.

---

## Running Tests

**Backend (Spring Boot):**

```bash
cd apps/api
./mvnw test
```

Run the full integration test against the real OpenAI API (requires `OPENAI_API_KEY` in env):

```bash
./mvnw -Dtest=GeneratePageOpenAiExternalIT test
```

Reset and re-migrate the local database:

```bash
./mvnw -Pdb-reset-local flyway:clean flyway:migrate
```

**Frontend (Next.js):**

```bash
cd apps/admin-web
npm run lint
npm run build
```

---

## Documentation

| Document | Description |
|---|---|
| [`docs/architecture.md`](docs/architecture.md) | System architecture, tech decisions, API endpoints |
| [`docs/data-model.md`](docs/data-model.md) | DTOs, domain records, enums |
| [`docs/ai-guardrails.md`](docs/ai-guardrails.md) | Ethical and regulatory constraints on AI output |
| [`docs/generation-flow.md`](docs/generation-flow.md) | End-to-end generation pipeline + HTTP contract |
| [`docs/prompt-template.md`](docs/prompt-template.md) | Prompt structure and JSON output schema |
| [`docs/landing-page-structure.md`](docs/landing-page-structure.md) | The 9-section page structure |
| [`docs/ai-questions.md`](docs/ai-questions.md) | The 7 guided form questions and their purpose |
| [`docs/persona.md`](docs/persona.md) | AI system identity, tone, and behavioral boundaries |
| [`docs/project-statement.md`](docs/project-statement.md) | Problem, solution, design principles |

---

## Environment Variables Reference

### `apps/api` (Spring Boot)

| Variable | Description |
|---|---|
| `OPENAI_API_KEY` | OpenAI API key — blank by default, runtime guard throws if missing at call time |
| `JWT_SECRET` | Base64url-encoded HS256 secret — shared with `admin-web`, must match |
| `SERVER_PORT` | HTTP port (default: `8080`) |

### `apps/admin-web` (Next.js)

| Variable | Description |
|---|---|
| `API_BASE_URL` | Spring Boot base URL |
| `AUTH_SECRET` | Auth.js session encryption secret |
| `JWT_SECRET` | HS256 secret for signing Spring-bound JWTs (must match backend) |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID (optional) |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret (optional) |
| `DATABASE_URL` | PostgreSQL connection string (used by Auth.js DB adapter) |
