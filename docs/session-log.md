# Session Log

## 2026-05-21

### Done Today

- **Admin platform Phase A** — read-only admin complet:
  - Flyway V9: `ALTER TABLE app_user ADD COLUMN is_admin BOOLEAN NOT NULL DEFAULT false`; jOOQ codegen re-run
  - `JwtService.generateToken(UUID, boolean isAdmin)` — claim `isAdmin` dans le JWT; `extractIsAdmin()` pour la lecture
  - `JwtAuthenticationFilter` — extrait le claim, attribue `ROLE_ADMIN` authority si admin
  - `AuthController` — login + oauth lisent `is_admin` depuis la DB avant de signer le JWT
  - `AdminController` (nouveau package `admin`) — `GET /api/admin/users` avec window function `ROW_NUMBER()` pour la latest page par user; `GET /api/admin/users/{userId}` réutilise les repos existants
  - `SecurityConfig` — `.requestMatchers("/api/admin/**").hasRole("ADMIN")`
  - `AdminControllerIntegrationTest` — 6 tests: 403 non-admin, 200 admin list, list contient user, detail ok, detail avec page, 404 user inconnu; suite totale 34/34 green
  - `next-auth.d.ts` — `isAdmin: boolean` dans Session/User/JWT
  - `auth.ts` — `decodeIsAdmin()` parse base64url du JWT Spring pour extraire le claim; stocké dans `token.isAdmin` + `session.user.isAdmin`
  - Proxy routes Next.js: `/api/admin/users/route.ts` + `/api/admin/users/[userId]/route.ts`
  - `api.ts` — types `AdminUserSummary` + `AdminUserDetail`, fonctions `getAdminUsers()` + `getAdminUserDetail()`
  - `app/admin/layout.tsx` — redirect `/` si non-admin
  - `app/admin/users/page.tsx` — table: email (lien), fullName, subdomain, status badge, date
  - `app/admin/users/[userId]/page.tsx` — card user + preview page read-only avec les 9 section renderers
  - `npm run build` clean — routes `/admin/users` et `/admin/users/[userId]` présentes
  - **Seed prod requis (manuel)**: `UPDATE app_user SET is_admin = true WHERE email = 'mednajib.slassi@gmail.com';`

- **Onboarding-first auth flow** — refactor de conversion : l'utilisateur remplit les 8 questions SANS auth (réduit friction d'entrée). Auth gate déclenché au "Continuer" du Summary (step 9):
  - `proxy.ts` : ajoute `onboarding` aux exclusions matcher
  - `app/page.tsx` : redirige vers `/onboarding` si non-authentifié, sinon `/page`
  - `app/onboarding/page.tsx` : auth optionnelle (`await auth().catch(() => null)`), passe `isAuthenticated` à `OnboardingFlow`
  - `flow.tsx` : `TOTAL_INPUT_STEPS=8`, `SUMMARY_STEP=9`, `PHOTO_STEP=10`. Photo déplacée APRÈS summary (post-auth car upload nécessite auth). `continueFromSummary()` : si pas authed → save state at step=10 + `router.push('/login?next=/onboarding')`, si authed → setStep(10)
  - `screens.tsx` : `SummaryScreen` perd `onGenerate`/`generating`/`error`, gagne `onContinue` (bouton "Continuer" au lieu de "Générer ma page"); `PhotoScreen` gagne `onGenerate`/`generating`/`error` (bouton "Générer ma page" + spinner inline); row "Photo" retirée du SUMMARY_ROWS
  - `login/page.tsx` + `register/page.tsx` : Suspense wrapper + `useSearchParams` pour récupérer `?next=`, propagé au Google OAuth callbackUrl + redirect credentials + lien register/login. Pattern : `next === "/" ? "/login" : "/login?next=..."`
  - `shell.tsx` : prop `isAuthenticated` (passée depuis FlowInner), affiche "Se connecter" (Link) ou "Se déconnecter" (signOut) selon état
- **Prod fix critique — Flyway en prod** : Spring Boot 4.0 a divisé les auto-configurations en modules séparés (`spring-boot-jdbc`, `spring-boot-jooq`, `spring-boot-security`, etc.). Sans `spring-boot-flyway` dans pom.xml, Flyway est dans le classpath mais Spring ne l'auto-configure pas → migrations ne tournent pas au démarrage de l'app, uniquement via le plugin Maven (`./mvnw flyway:migrate`). Symptôme : sur Railway, après recréation du service Postgres, l'app démarrait en 3.6s sans logs HikariCP/Flyway, et `UserDetailsServiceAutoConfiguration` se déclenchait (warning generated security password) en parallèle (mais c'était inoffensif, notre SecurityFilterChain était bien actif). Fix : ajout de `<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-flyway</artifactId></dependency>`. Debug via `java -jar app.jar --debug` + grep "Flyway"/"Hikari" dans le rapport d'auto-config.
- **Subdomain wildcard deploy** : nameservers délégués à Vercel (`ns1.vercel-dns.com`, `ns2.vercel-dns.com` chez Hostinger). DNS records dans Vercel : A `@` → `76.76.21.21`, CNAME `www` → `cname.vercel-dns.com`, CNAME `*` → `cname.vercel-dns.com`. Wildcard `*.ethorai.fr` nécessite délégation NS (gratuit) OU Vercel Pro (avec DNS externe). Bug bonus fixé : middleware capturait `www` comme slug → ajout d'une liste `RESERVED = ["www", "api", "admin", "mail", "smtp", "ftp"]`. Bug bonus 2 : `API_BASE_URL` manquait `https://` dans Vercel env vars → erreur `ERR_INVALID_URL`.
- **Bouton "Se déconnecter" pendant l'onboarding** : ajouté dans le header `Shell` (auparavant aucun moyen de logout depuis onboarding)

### Next 3 Tasks

1. Seed `is_admin = true` en prod pour le compte fondateur (SQL ci-dessus)
2. Lancer les 5 interviews thérapeutes (voir `docs/user-interviews.md`)
3. Vercel Analytics (post-interview)

### Current Blocker

Aucun — 34/34 tests, build clean. Seule étape manuelle : SQL prod pour activer l'admin.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From `apps/api`:
`./mvnw spring-boot:run`

From `apps/admin-web`:
`npm run dev`

Naviguer vers `/admin/users` après seed SQL prod.

---

## 2026-04-26

### Done Today

- **Sticky public header + enriched section content** — addressed user feedback that AREAS_OF_SUPPORT cards looked like "a list of user choices in rectangles" (just "Stress" inside a box):
  - `HeaderSection` now `sticky top-0 z-10` with `backdrop-blur-md` (workspace bar at z-20 takes precedence on `/page`)
  - Backend data shape: `AreasOfSupportData.items: List<String>` → `List<Item>` with nested `Item(title, description)` record; same for `WhatYouCanExpectData.statements` → `List<Statement>`
  - Prompt: AI now instructed to generate `{title: "topic (3-6 words)", description: "1-2 sentences professional, neutral"}` per item, both in main prompt and per-section regeneration schema
  - Mapper: backward-compat parser handles both old shape (List<String>) and new shape (List<Item>) — existing dev DB pages won't 502, they'll just have empty descriptions until regenerated
  - Test fixture (`StructuredSectionsBuilder`): updated with realistic title+description pairs in French
  - External OpenAI test (`GeneratePageOpenAiExternalIT`): assertions updated from `items[0]` (string) to `items[0].title` + `items[0].description`
  - Frontend: new `AreaOfSupportItem` and `ExpectationStatement` types; rich card rendering (h3 title + body description); `RichItemListEditor` replaces `StringListEditor` in section-editors (still hidden in v1, but compiles & functions)
  - 28/28 tests; lint + build green
- **Public page redesign (Phase 3)** — rewrote `section-renderers.tsx` (9 components) to match Ethorai brand:
  - Palette: warm cream + stone neutrals, no more blue/gray Bootstrap clichés
  - Typography: Fraunces serif on headings (auto via h-tags) + Space Grotesk body; added `--font-serif` Tailwind theme var for inline serif on non-headings (used in HEADER name + FOOTER name)
  - Hero: radial gradient `#fbe3d3 → #f7f5ee → #f4efe4`, `text-6xl font-medium` (vs `font-bold`), `py-32`
  - Section rhythm: alternate `bg-white` ↔ `bg-stone-50/60`; CONTACT uses warm linear gradient anchor; FOOTER soft `bg-stone-100`
  - DISCLAIMER softened from amber-warning to italic stone-500 aside
  - Functional CTA: `<a>` with `mailto:` / `tel:` fallback (gap noted: BOOKING_LINK URL not exposed in `ContactData`, deferred to Phase 4)
- **Workspace + cleanup (Phase 1+2)** — collapsed multi-page admin to singleton:
  - Backend: `LandingPageRepository.findLatestByUserId(UUID)` + `MeController` with `GET /api/me/page` and `GET /api/me/profile` (204 No Content when empty); `MeControllerIntegrationTest` (4 tests, total suite now 28/28)
  - Frontend proxies: `/api/me/page`, `/api/me/profile`
  - Frontend client: `getMyPage()`, `getMyProfile()` (return `null` on 204)
  - New `/page` route: server component fetches via JWT direct to Spring, redirects to `/onboarding` if 204
  - New `Workspace` client component: sticky top bar (Ethorai brand + status pill DRAFT/PUBLISHED + actions); section editors hidden but code preserved; spinner overlay during regenerate
  - Actions: **Modifier mes réponses** (loads profile into localStorage as OnboardingState step=1, pushes /onboarding) · **Régénérer** (POST /api/generate with current profile + poll + router.refresh) · **Publier ma page** (POST /api/pages/{id}/publish + auto-copy public URL + toast) · **Voir comme un visiteur** (opens /p/{id}) · **Copier le lien** · **Se déconnecter** (signOut from next-auth/react)
  - Deleted: `app/dashboard/`, `app/generate/`, `app/pages/[id]/`, `app/api/pages/route.ts` (LIST endpoint by profileId)
  - Updated redirects: `/` → `/page`, login + register success → `/` (which routes intelligently)
  - Onboarding flow's post-generation push changed from `/pages/{id}` → `/page`
  - Test isolation gotcha fixed: `MeController` test cleanup deletes from `generation_job` first (FK), then `landing_page`
- **Conversational onboarding flow** at `/onboarding` (Typeform-style, one question per screen):
  - 10 screens: welcome + 8 input questions + summary, all in French, vouvoyé tone
  - Maps 1:1 to existing `TherapistInput` (fullName, location, role, audiences, areasOfSupport, approach, sessionFormat, expectations, contactMethod, contactValue) — no backend changes
  - localStorage persistence (`ethorai:onboarding` key) — auto-save on every change, restore on mount, cleared after successful generation
  - Two-stage component (`OnboardingFlow` → `FlowInner`) to keep state hydration isolated and avoid SSR mismatches
  - Reusable primitives: `Chip`, `RadioCard`, `PrimaryButton`, `BackArrow`, `ArrowRight` (all Tailwind, no UI lib)
  - Progress bar in shell, back button conditional, Enter-key submission on text inputs
  - Submits via existing `generateLandingPage()` → polls `getGenerationStatus()` → routes to `/pages/[id]` on completion
  - Error states + per-row "Modifier" links from summary back to specific steps
- Root `/` redirects to `/onboarding` instead of `/dashboard` (new entry point post-login)
- Files: `lib/onboarding-storage.ts`, `components/onboarding/{flow,shell,primitives,screens}.tsx`, `app/onboarding/page.tsx`, modified `app/page.tsx`
- Verified: `npm run lint` clean, `npm run build` green
- Product consulting: validated the decision NOT to redo the 9-section page structure now — too early without therapist feedback. Onboarding refonte stays scope-limited to UI; backend untouched
---
- Named project **Ethorai** — created GitHub org `ethorai`, pushed monorepo as `ethorai/ethorai-monorepo`
- Deployed Spring API to **Railway** (Spring + managed PostgreSQL 17):
  - Fixed `server.port` silently ignored (was nested under `spring:` in application.yaml — moved to root)
  - Fixed `DATABASE_URL` missing `jdbc:` prefix — switched to Railway individual vars (`PGHOST/PGPORT/PGDATABASE/PGUSER/PGPASSWORD`) in datasource URL
  - Added `${PORT:${SERVER_PORT:8080}}` so Railway's injected `PORT` var takes effect
- Deployed Next.js admin to **Vercel**:
  - Fixed `middleware.ts` silently ignored by Next.js 16 — renamed to `proxy.ts` (new convention); dashboard was publicly accessible on Vercel
  - Fixed `MissingSecret` Auth.js error — `AUTH_SECRET` was not set in Vercel env vars
  - Fixed `client_id=undefined` in Google OAuth URL — Auth.js v5 reads `AUTH_GOOGLE_ID` by default but we pass credentials explicitly via `Google({clientId: process.env.GOOGLE_CLIENT_ID, clientSecret: process.env.GOOGLE_CLIENT_SECRET})`
  - Fixed Google OAuth 401 `invalid_client` — added `https://ethorai.vercel.app/api/auth/callback/google` as authorized redirect URI in Google Cloud Console
  - Fixed `NEXT_PUBLIC_API_URL` → `API_BASE_URL` in register proxy route
- Fixed 401 on `POST /api/generate` after Google login:
  - Root cause: `INTERNAL_SECRET` missing from local Spring environment — `internalSecret.isBlank()` returned true → `POST /api/auth/oauth` rejected → `springToken` never stored in session
  - Created `DotEnvEnvironmentPostProcessor` (registered via `META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor.imports`) — loads `apps/api/.env` before context startup; OS env vars take precedence so Railway is unaffected
  - Added `INTERNAL_SECRET` to `apps/api/.env`
- Added IntelliJ `API - jOOQ Regenerate` run config for `jooq-codegen:generate`
- Fixed duplicate jOOQ class compile error with `./mvnw clean`
- Fixed `Forbidden term detected: résultats` surfacing as 422 to user — added `generateWithRetry()` in `GenerationOrchestrator`: retries AI generation up to 3 times on `GenerationValidationException` (GPT-4o forbidden term slippage); only fails if all 3 attempts fail validation
- Updated CLAUDE.md + `.github/copilot-instructions.md`: commit protocol now requires updating both tracking files in every commit, not just end-of-session
- 24/24 tests passing throughout

## 2026-05-02

### Done Today

- **Fix publish 502** — `revalidatePath` was inside the `try/catch` in `POST /api/pages/[id]/publish/route.ts`; if it threw (Next.js 16 context issue), the catch returned 502 even though Spring had already returned 204. Split fetch errors and revalidation into separate try/catch blocks; added `console.error` logging for each.
- **Multi-contact onboarding** (carried over from previous session) — replaced single `contactMethod/contactValue` with three independent nullable fields (`phone`, `email`, `bookingLink`):
  - Flyway V5: DROP old columns, ADD new columns with data migration
  - `TherapistInput` Java record updated; `@AssertTrue` cross-field validation
  - `TherapistProfileRepository`, `GenerationOrchestrator`, `MeController`, `InputNormalizationService`, `PromptAssemblyService`, `ContactData`, `StructuredSectionsMapper` all updated
  - jOOQ generated sources manually updated (V5 migration applied + codegen re-run)
  - `ContactScreen` rewritten: multi-select toggle cards (email / phone / Calendly), French phone auto-formatter `06 12 34 56 78`
  - `ContactSection` renderer: CTA prefers booking_link → mailto → tel; secondary contacts row below CTA
  - All 28 tests updated and passing
- **Photo upload** — therapist profile photo end-to-end:
  - Flyway V6: `ADD COLUMN photo_url VARCHAR(500)` on `therapist_profile`
  - jOOQ codegen re-run against migrated DB
  - `TherapistInput` + `GeneratedPageResponse` + all controllers + repository + orchestrator updated to carry `photoUrl`
  - Next.js upload route `POST /api/upload/photo` → Vercel Blob (5 MB limit, JPEG/PNG/WebP only, auth-gated)
  - New `PhotoScreen` (step 9) in onboarding — click-to-upload with preview, optional (has "Passer" button)
  - `HeroSection` redesigned: side-by-side layout (text left, circular photo right on desktop; stacked on mobile)
  - `next/image` + `remotePatterns` for `*.public.blob.vercel-storage.com`
  - `BLOB_READ_WRITE_TOKEN` env var required in admin-web (Vercel Blob)
  - All backend tests passing (BUILD SUCCESS); Next.js build clean

## 2026-05-04

### Done Today

- **Wildcard subdomain feature** (`marie-dupont.ethorai.fr`) — full-stack:
  - Flyway V8: `ADD COLUMN subdomain VARCHAR(100) UNIQUE` on `therapist_profile`
  - jOOQ codegen re-run against migrated DB (linter revert issue → always run codegen, never edit generated files manually)
  - `TherapistProfileRepository`: `insert()` prend `String subdomain`, `findBySubdomain()`, `subdomainExists()`, `TherapistProfileRow` inclut `subdomain`
  - `GenerationOrchestrator`: `generateUniqueSubdomain()` — normalise accents (é→e, à→a, etc.) + slugify + suffixe numérique `-2`, `-3`... si conflit
  - `GeneratedPageResponse`: champ `subdomain` ajouté (backend + `api.ts` frontend)
  - `PublicPageController`: nouveau endpoint `GET /api/public/pages/subdomain/{slug}`
  - Next.js `/s/[slug]/page.tsx`: ISR server component fetching `GET /api/public/pages/subdomain/{slug}`
  - `proxy.ts` middleware: détecte `*.ethorai.fr` → rewrite vers `/s/{slug}`, sinon auth normal; `/s/` exclu du matcher auth
  - Workspace: "Voir comme un visiteur" et "Copier le lien" utilisent `https://{subdomain}.ethorai.fr` quand disponible
  - 28/28 tests green; Next.js build clean

- **Conformité RGPD/LCEN** — quick win pré-interview:
  - Page `/mentions-legales` statique: éditeur (Mohamed Najib Slassi + email), hébergeurs (Vercel + Railway), données personnelles RGPD, cookies (session only, exemptés), mention OpenAI
  - Notice discrète (`text-xs`) sur `/login` ET `/register` — couvre les utilisateurs Google qui ne passent jamais par le register
  - Lien "Mentions légales" dans le footer des pages publiques `/p/[id]`, à côté de "Page générée avec Ethorai"
- **Prompt quality** — deux améliorations de génération:
  - `SESSION_FORMATS`: quand BOTH, toujours 2 entrées séparées ("En cabinet" + "En visio") — jamais une seule entrée "Les deux". Même correction dans le prompt de régénération de section
  - `WHAT_YOU_CAN_EXPECT`: descriptions ne doivent plus paraphraser le titre (ex: titre "Bienveillance" → description "Un environnement bienveillant..."). Prompt force maintenant "répondre concrètement à ce que ça veut dire en séance"
  - `humanizeFormatType` côté frontend: fallback pour les pages déjà générées avec "Les deux" → "En cabinet et en visio"
- **UX fixes**:
  - Dropdown autocomplete adresse masquée par `overflow-hidden` du parent — déplacé sur la classe `max-h-0` seulement (visible quand ouvert)
  - `LocationMapSection` ajouté au workspace `/page` — la carte est visible avant de publier
  - Google Maps embed restauré (était passé à OSM temporairement)

## 2026-05-03

### Done Today

- **Structured address columns (V7)** — full-stack address refactor:
  - Flyway V7: `RENAME location → city`, ADD `street_address VARCHAR(255)`, `postal_code VARCHAR(10)`, `latitude DOUBLE PRECISION`, `longitude DOUBLE PRECISION`
  - jOOQ codegen re-run against migrated DB
  - Backend: `TherapistInput`, `GeneratedPageResponse`, `TherapistProfileRepository`, `GenerationOrchestrator`, `MeController`, `PageController`, `PublicPageController`, `InputNormalizationService`, `PromptAssemblyService` all updated (`location()` → `city()`, 4 new address fields propagated end-to-end)
  - All 7 test files updated (constructor arity +4 nulls); `MeControllerIntegrationTest` assertion updated to `$.city`
  - 28/28 tests green; `./mvnw compile test-compile` clean
  - Frontend: `api.ts` (`TherapistInput` + `GeneratedPageResponse` updated), `onboarding-storage.ts` (`location → city`, 4 new fields), `flow.tsx` (payload), `workspace.tsx` (seeded state)
  - `IdentityScreen` in `screens.tsx`: renamed `state.location → state.city`, added address autocomplete widget using French gov API (`api-adresse.data.gouv.fr`) with 300ms debounce — picks city, streetAddress, postalCode, lat/lng from suggestion
  - Added `LocationMapSection` to `section-renderers.tsx` (Google Maps iframe embed, no API key)
  - Public page `/p/[id]`: renders `LocationMapSection` only when `sessionFormat` is IN_PERSON or BOTH and location data is present
  - Next.js build clean (TypeScript + static generation)

### Next 3 Tasks

1. Config DNS + Vercel : CNAME wildcard `*.ethorai.fr` → `cname.vercel-dns.com` + ajouter domaine wildcard `*.ethorai.fr` dans Vercel project settings
2. Deploy sur Railway + Vercel (V8 migration auto au démarrage)
3. Lancer les 5 interviews thérapeutes (voir `docs/user-interviews.md`)

### Current Blocker

Aucun — build clean, 28/28 tests. Étape manuelle requise : config DNS wildcard + Vercel domain.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From `apps/api`:
`./mvnw spring-boot:run`

From `apps/admin-web`:
`npm run dev`

---

## 2026-05-02

### Done Today

- **SESSION_FORMATS polish**: removed redundant "Format" eyebrow label from inside each session format card in `SessionFormatsSection` — it duplicated the section title "Formats des sessions"
- **SESSION_FORMATS humanize**: added `humanizeFormatType()` helper mapping raw enum values (ONLINE/IN_PERSON/BOTH) to human-friendly French labels; updated prompt in `PromptAssemblyService`
- **Multi-contact + phone formatting** — full-stack change replacing single `contactMethod`/`contactValue` with three independent contact channels:
  - DB: V5 migration adds `phone`, `email`, `booking_link` nullable columns; migrates existing data; drops old `contact_method`/`contact_value`
  - Backend: `TherapistInput` (phone/email/bookingLink + `@AssertTrue` validation), `ContactData` (added `booking_link`), `TherapistProfileRepository`, `InputNormalizationService`, `GenerationOrchestrator`, `MeController`, `PromptAssemblyService` (both prompts + section schema), `StructuredSectionsMapper` (fallback); jOOQ generated files updated manually
  - Frontend: `OnboardingState` (contactPhone/contactEmail/contactBookingLink), `TherapistInput` API type, `ContactData` API type, `workspace.tsx` "Modifier" pre-fill; `ContactSection` renderer (booking link CTA with `target=_blank`, secondary contact row)
  - Onboarding `ContactScreen`: 3 toggle cards (multi-select); toggle expands input inline; phone auto-formats as XX XX XX XX XX on keypress; canContinue requires at least one non-empty value; summary row shows all contacts separated by ·
  - 28/28 tests green; `npm run build` green

### Next 3 Tasks

1. Browser test the full flow: register → onboarding → /page workspace → publish → /p/[id] · Modifier mes réponses · Régénérer · Se déconnecter
2. 5 user interviews with target therapists to validate the generated page output (per product consulting)
3. Calendly/Doctolib booking link: verify AI properly passes it through to `booking_link` in generated CONTACT section

### Current Blocker

None.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From `apps/api`:
`./mvnw spring-boot:run`

From `apps/admin-web`:
`npm run dev`

---

## 2026-04-20

### Done Today

- XSS audit on `section-renderers.tsx`: confirmed all 9 section renderers use JSX interpolation only — no `dangerouslySetInnerHTML` anywhere in admin-web. No fix needed.
- GitHub Actions CI pipeline: `.github/workflows/ci.yml`
  - `api` job: PostgreSQL 17 service container on port 5433, Java 21 (temurin), `./mvnw test`
  - `admin-web` job: Node 20, `npm ci` + `npm run lint` + `npm run build`
- Public page route end-to-end:
  - Spring: `LandingPageRepository.findByIdPublic()` (no userId, PUBLISHED-only filter), `PublicPageController` at `/api/public/pages/{id}`, `SecurityConfig` permits `/api/public/**`
  - Next.js: `/p/[id]/page.tsx` ISR Server Component — fetches from public Spring endpoint, renders all 9 section components, `revalidate=3600`, `generateMetadata` for SEO
  - Publish proxy: calls `revalidatePath('/p/{id}')` after Spring returns 204
- Fixed pre-existing build failure: login page `useSearchParams` extracted into `<RegisteredBanner>` component wrapped in `<Suspense>` (Next.js 16 requirement)
- Dockerfile (multi-stage, eclipse-temurin:21-jdk builder + 21-jre runtime) + `.dockerignore` — Docker build verified locally
- jOOQ generated sources moved to `src/main/generated/jooq` and committed — `build-helper-maven-plugin` registers them as compile source root; `jooq.codegen.skip` property added for Docker and CI
- CI updated: `./mvnw test -Djooq.codegen.skip=true` (DB still used by tests; codegen skipped since sources are committed)
- Auth refactor — Spring is now the single auth authority:
  - `JwtService.generateToken(UUID)` added — 7-day HS256 JWT
  - `POST /api/auth/login` — validates credentials, issues JWT
  - `POST /api/auth/oauth` — upserts Google user + oauth_account, issues JWT, protected by `X-Internal-Secret` header
  - `auth.ts` rewritten — calls Spring for both credential and Google flows, no pg/bcrypt imports, no DB access
  - `spring-fetch.ts` simplified — reads `session.user.springToken` directly, no JWT generation in Next.js
  - `DATABASE_URL` no longer needed on Vercel
  - Fixed `NEXT_PUBLIC_API_URL` → `API_BASE_URL` in register proxy
- 24/24 Spring tests passing, Next.js build clean

### Next 3 Tasks

1. Deploy to Railway + Vercel (add INTERNAL_SECRET env var to both)
2. Wire "View public page" link on admin page detail (`/pages/[id]`) pointing to `/p/{id}`
3. (Later) Port infra to AWS ECS + RDS as a portfolio infrastructure exercise

### Current Blocker

None.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From `apps/api`:
`./mvnw spring-boot:run`

From `apps/admin-web`:
`npm run dev`

---

## 2026-04-16 (session 4)

### Done Today

- Added prompt injection sanitization to `InputNormalizationService`:
  - Strips `IGNORE previous instructions`, `DISREGARD all`, `OVERRIDE the system` patterns
  - Strips prompt role markers (`SYSTEM:`, `USER:`, `ASSISTANT:` at line start)
  - Strips separator injection lines (`---`, `===`, `***` on own lines)
  - Collapses multiple newlines/whitespace to single space
  - Adds length limits: `approach` max 500 chars, other text fields max 150 chars
- `InputNormalizationServiceTest`: 7 unit tests covering injection patterns and length limits
- 24/24 tests passing (up from 17)

### Next 3 Tasks

1. Verify `contactValue` is HTML-escaped on generated pages
2. GitHub Actions CI pipeline
3. Deploy to Railway + Vercel

### Current Blocker

None.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From `apps/api`:
`./mvnw spring-boot:run`

From `apps/admin-web`:
`npm run dev`

---

## 2026-04-16 (session 3)

### Done Today

- Added per-user rate limiting on `POST /api/generate` using Bucket4j (5 requests/user/hour)
- `GenerationRateLimiter` — `@Component`, `ConcurrentHashMap<UUID, Bucket>`, 5 tokens/hour refill
- `RateLimitExceededException` + 429 handler in `GenerationExceptionHandler`
- Wired `rateLimiter.consume(userId)` in `GenerationController` before `submitAsync`
- Tests: `GenerationRateLimiterTest` (3 unit tests) + `RateLimitIntegrationTest` (2 integration tests)
- 17/17 tests passing (up from 12)

### Next 3 Tasks

1. Sanitize free-text inputs against prompt injection (`approach` and other free-text fields)
2. Verify `contactValue` is HTML-escaped on generated pages
3. GitHub Actions CI pipeline

### Current Blocker

None.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From `apps/api`:
`./mvnw spring-boot:run`

From `apps/admin-web`:
`npm run dev`

---

## 2026-04-16 (session 2)

### Done Today

- Full project review: architecture, docs, security gaps, monetization, priorities
- Fixed docs/persona.md: removed 170-line ChatGPT conversation transcript accidentally left in file
- Fixed docs/architecture.md: replaced stale "no auth for MVP" decision, corrected static deploy to ISR model, promoted section-level regen from "future" to live endpoints, updated async generation flow
- Fixed docs/data-model.md: replaced JPA `@Entity`/`@Enumerated`/`@ManyToOne` with jOOQ-correct Java records, replaced `Map<SectionType, String>` with `StructuredSections` throughout
- Fixed docs/prompt-template.md: replaced text-based output format with actual JSON schema matching the parser
- Created README.md: full portfolio-grade README with architecture, tech decisions, local setup, key commands, env vars reference
- Created CLAUDE.md: project instructions for Claude Code with tech stack, key classes, auth architecture, testing conventions, env vars, end-of-session protocol

### Next 3 Tasks

1. Rate limiting on `POST /api/generate` (per-user, prevents OpenAI cost abuse)
2. Sanitize free-text inputs against prompt injection (`approach` and other free-text fields)
3. GitHub Actions CI pipeline (`./mvnw test` + `npm run build`)

### Current Blocker

None.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From `apps/api`:
`./mvnw spring-boot:run`

From `apps/admin-web`:
`npm run dev`

---

## 2026-04-16

### Done Today

- Fixed middleware auth guard (was silently skipped, dashboard accessible without login):
  - Split `auth.ts` → edge-safe `auth.config.ts` (no bcrypt/pg imports) + full `auth.ts` spreads it
  - Moved `middleware.ts` from project root into `src/` (Next.js `src/` directory convention)
  - Middleware now uses explicit `req.auth` check with redirect to `/login`
  - Added `session: { strategy: "jwt" }` to auth config for correct cookie parsing
- Root `/` now redirects to `/dashboard` (removed marketing splash)
- Formatter-applied style fixes to login + register pages

### Next 3 Tasks

1. README with architecture, tech decisions, local setup guide
2. GitHub Actions CI pipeline
3. Deploy (Vercel + Railway/Fly.io)

### Current Blocker

Google OAuth requires `GOOGLE_CLIENT_ID` + `GOOGLE_CLIENT_SECRET` in `apps/admin-web/.env.local`.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From [apps/api](../apps/api):
`./mvnw spring-boot:run`

From [apps/admin-web](../apps/admin-web):
`npm run dev`

---

## 2026-04-12

### Done Today

- Credentials sign-up flow end-to-end:
  - **Spring:** `AuthController` `POST /api/auth/register` (public — no JWT), `RegisterRequest` with `@NotBlank @Email @Size(min=8)` validation, `BCryptPasswordEncoder` hashing, 409 on duplicate email; `SecurityConfig` permits `/api/auth/register`
  - **Next.js:** `/api/register` proxy route (201/409/400/502 mapping), `/register` page (name + email + password, auto sign-in on success), `/login` "Create account" link + `?registered=1` green banner, middleware excludes `/register` and `/api/register`
- 12/12 Spring tests green, TypeScript clean

### Next 3 Tasks

1. README with architecture, tech decisions, local setup guide
2. GitHub Actions CI pipeline (`./mvnw test`)
3. Deploy (Vercel + Railway/Fly.io)

### Current Blocker

Google OAuth requires `GOOGLE_CLIENT_ID` + `GOOGLE_CLIENT_SECRET` in `apps/admin-web/.env.local`.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From [apps/api](../apps/api):
`./mvnw spring-boot:run`

From [apps/admin-web](../apps/admin-web):
`npm run dev`

---

## 2026-04-10

### Done Today

- Completed userId scoping for page endpoints (row-level security enforcement):
  - **SecurityContextHelper.java** — new helper, extracts UUID from Spring SecurityContext
  - **LandingPageRepository.java** — all 5 methods (`insert`, `findById`, `findByProfileId`, `updateSection`, `updateStatus`) now take `UUID userId` with `WHERE user_id = :userId`
  - **GenerationController.java** — calls `SecurityContextHelper.currentUserId()`, passes to `orchestrator.submitAsync(input, userId)`
  - **PageController.java** — every endpoint extracts userId, passes to all repo calls and `orchestrator.regenerateSection(id, userId, sectionType)`
  - **GenerationOrchestrator.java** — removed dead `execute()` method; `regenerateSection` now takes `UUID userId`, repo calls updated to pass userId
  - **Tests** — `@WithMockUser(username = "00000000-0000-0000-0000-000000000001")` on all 4 test classes; `@BeforeEach` inserts test user via jOOQ upsert (`ON CONFLICT DO NOTHING`) in `GeneratePageIntegrationTest` and `GetPageIntegrationTest`
- All 12 tests passing, BUILD SUCCESS

### Next 3 Tasks

1. User registration flow (credentials sign-up page in admin-web)
2. Magic link provider via Resend
3. Role-specific disclaimer templates and stricter semantic checks

### Current Blocker

Google OAuth requires `GOOGLE_CLIENT_ID` + `GOOGLE_CLIENT_SECRET` in `apps/admin-web/.env.local` — user needs to register an OAuth app at `console.cloud.google.com`.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From [apps/api](../apps/api):
`./mvnw test`

From [apps/admin-web](../apps/admin-web):
`npm run dev`

---

## 2026-04-06

### Done Today

- Set up Prettier + ESLint VS Code workspace config:
  - `.vscode/settings.json` with `eslint.useFlatConfig`, `changeProcessCWD`, `eslint.validate`
  - `.vscode/extensions.json` recommending ESLint + Prettier extensions
  - `apps/admin-web/.prettierrc` with standard settings
  - Added `eslint-config-prettier` to prevent formatting rule conflicts
- Implemented full auth layer — Spring Security (resource guard) + Auth.js v5 (identity layer):
  - **Spring Security:**
    - [SecurityConfig.java](../apps/api/src/main/java/com/ai/therapists/api/security/SecurityConfig.java) — stateless JWT filter chain, all routes authenticated
    - [JwtAuthenticationFilter.java](../apps/api/src/main/java/com/ai/therapists/api/security/JwtAuthenticationFilter.java) — `OncePerRequestFilter`: extracts Bearer token, validates, sets userId
    - [JwtService.java](../apps/api/src/main/java/com/ai/therapists/api/security/JwtService.java) — HS256 validation via JJWT, base64url secret
    - Added `spring-boot-starter-security` + JJWT dependencies to pom.xml
    - Added `spring-security-test` for `@WithMockUser` support
  - **DB migrations:**
    - [V3\_\_auth_schema.sql](../apps/api/src/main/resources/db/migration/V3__auth_schema.sql) — `app_user`, `oauth_account`, `verification_token` tables
    - [V4\_\_landing_page_user.sql](../apps/api/src/main/resources/db/migration/V4__landing_page_user.sql) — `landing_page.user_id` FK (nullable)
  - **Auth.js v5 (Next.js):**
    - [auth.ts](../apps/admin-web/src/auth.ts) — NextAuth config with Google OAuth + Credentials providers, JWT session strategy
    - [db-adapter.ts](../apps/admin-web/src/lib/db-adapter.ts) — custom DB adapter for `app_user` / `oauth_account`
    - [spring-auth.ts](../apps/admin-web/src/lib/spring-auth.ts) — signs short-lived HS256 JWT via `jose` for Spring calls
    - [spring-fetch.ts](../apps/admin-web/src/lib/spring-fetch.ts) — `withAuth()` helper: resolves session → returns auth headers
    - [route.ts](../apps/admin-web/src/app/api/auth/%5B...nextauth%5D/route.ts) — Auth.js catch-all route handler
    - [next-auth.d.ts](../apps/admin-web/src/types/next-auth.d.ts) — session type augmentation with `user.id`
  - **Login page + route guard:**
    - [login/page.tsx](../apps/admin-web/src/app/login/page.tsx) — Google button + email/password form, warm stone palette
    - [middleware.ts](../apps/admin-web/middleware.ts) — redirects unauthenticated users to `/login`
  - **JWT forwarding:** all 7 proxy routes updated to call `withAuth()` and pass `Authorization: Bearer` to Spring
  - **Test fixes:** all 4 test classes updated with `@WithMockUser` + `MockMvcBuilders.webAppContextSetup().apply(springSecurity())`
- Verified everything:
  - `./mvnw test` → 12 tests passed, exit 0
  - `npx tsc --noEmit` → exit 0
  - `npx next build` → exit 0, all routes visible including `/login` and `/api/auth/[...nextauth]`
  - `npx eslint` → clean on all new files

### Next 3 Tasks

1. Scope page endpoints by userId (landing_page.user_id enforcement in Spring)
2. User registration flow (credentials sign-up page)
3. Magic link provider via Resend

### Current Blocker

Google OAuth requires `GOOGLE_CLIENT_ID` + `GOOGLE_CLIENT_SECRET` in `apps/admin-web/.env.local` — user needs to register an OAuth app at `console.cloud.google.com`.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From [apps/api](../apps/api):
`./mvnw test`

From [apps/admin-web](../apps/admin-web):
`npm run dev`

---

## 2026-03-31

### Done Today

- Completed the response contract cleanup so backend page APIs now return typed structured sections directly instead of raw JSON strings:
  - [GeneratedPageResponse.java](../apps/api/src/main/java/com/ai/therapists/api/page/GeneratedPageResponse.java)
  - [PageController.java](../apps/api/src/main/java/com/ai/therapists/api/page/PageController.java)
  - [GenerationOrchestrator.java](../apps/api/src/main/java/com/ai/therapists/api/generation/GenerationOrchestrator.java)
- Added backend response mapping with legacy draft fallback so older stored HTML/string sections can still be read through the new structured contract:
  - [StructuredSectionsMapper.java](../apps/api/src/main/java/com/ai/therapists/api/page/StructuredSectionsMapper.java)
- Removed transitional client-side parsing from admin-web and aligned all main screens to consume structured sections directly:
  - [src/lib/api.ts](../apps/admin-web/src/lib/api.ts)
  - [src/app/generate/page.tsx](../apps/admin-web/src/app/generate/page.tsx)
  - [src/app/dashboard/page.tsx](../apps/admin-web/src/app/dashboard/page.tsx)
  - [src/app/pages/[id]/page.tsx](../apps/admin-web/src/app/pages/%5Bid%5D/page.tsx)
- Centralized section save serialization in the frontend API client so page detail editors send structured JSON consistently:
  - [src/lib/api.ts](../apps/admin-web/src/lib/api.ts)
- Updated the optional real OpenAI integration test to assert nested structured fields instead of string section bodies:
  - [GeneratePageOpenAiExternalIT.java](../apps/api/src/test/java/com/ai/therapists/api/GeneratePageOpenAiExternalIT.java)
- Verified both apps after the contract migration:
  - `./mvnw test` → exit 0
  - `npm run build` → exit 0
- Implemented section-level regeneration endpoint:
  - Added focused regeneration prompt in [PromptAssemblyService.java](../apps/api/src/main/java/com/ai/therapists/api/generation/PromptAssemblyService.java)
  - Added `regenerateSection()` in [AiGenerationService.java](../apps/api/src/main/java/com/ai/therapists/api/generation/AiGenerationService.java)
  - Added `regenerateSection()` orchestration in [GenerationOrchestrator.java](../apps/api/src/main/java/com/ai/therapists/api/generation/GenerationOrchestrator.java)
  - Added `POST /api/pages/{id}/sections/{sectionType}/regenerate` in [PageController.java](../apps/api/src/main/java/com/ai/therapists/api/page/PageController.java)
  - Added proxy route [regenerate/route.ts](../apps/admin-web/src/app/api/pages/%5Bid%5D/sections/%5BsectionType%5D/regenerate/route.ts)
  - Added `regenerateSection()` client function in [api.ts](../apps/admin-web/src/lib/api.ts)
  - Wired "Regenerate" button per section in [pages/[id]/page.tsx](../apps/admin-web/src/app/pages/%5Bid%5D/page.tsx)
- Verified both apps after regeneration feature:
  - `./mvnw test` → exit 0
  - `npm run build` → exit 0
- Implemented richer save UX on page detail view:
  - Per-section inline feedback (success/error shown next to each section editor)
  - Auto-dismiss success messages after 3 seconds
  - Dirty state tracking with amber highlight and dot indicator on unsaved sections
  - Browser `beforeunload` warning when navigating away with unsaved changes
  - Removed global save/error banners in favor of per-section feedback
- Verified frontend build after save UX changes:
  - `npm run build` → exit 0
- Implemented async generation + status polling:
  - Flyway V2 migration: `generation_job` table (id, profile_id, page_id, status, error, timestamps)
  - `GenerationJobStatus` enum (PENDING, IN_PROGRESS, COMPLETED, FAILED)
  - `GenerationJobRepository` with insert/findById/updateStatus/markCompleted/markFailed
  - `GenerationJobResponse` record for API responses
  - `GenerationOrchestrator.submitAsync()` — creates profile + job, launches `@Async processJobAsync()`
  - `GenerationOrchestrator.getJobStatus()` — returns job state
  - `@EnableAsync` on `ApiApplication`
  - `GenerationController` refactored: `POST /api/generate` → 202 Accepted with job response, `GET /api/generate/status/{jobId}` → job status
  - `IllegalArgumentException` handler in `GenerationExceptionHandler` for 404
  - Frontend proxy route `GET /api/generate/status/[jobId]`
  - `generateLandingPage()` now returns `GenerationJobResponse`, added `getGenerationStatus()` to API client
  - Generate page updated: submit → poll every 2s → fetch page on completion, spinner + status indicator in preview
  - All 4 test classes updated for async flow (POST → 202, poll until COMPLETED/FAILED)
- Verified after async generation feature:
  - `./mvnw test` → 12 tests passed, exit 0
  - `npm run build` → exit 0

### Next 3 Tasks

1. Auth + billing + custom domains.
2. CI pipeline with automated integration test run.
3. Role-specific disclaimer templates and stricter semantic checks.

### Current Blocker

No blocker.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From [apps/api](../apps/api):
`./mvnw test`

From [apps/admin-web](../apps/admin-web):
`npm run dev`

---

## 2026-03-28

### Done Today

- Defined current admin HTTP contract in existing documentation:
  - [docs/generation-flow.md](generation-flow.md)
  - Covers `POST /api/generate`, `GET /api/pages/{id}`, `GET /api/pages?profileId=...`, `PUT /api/pages/{id}/sections/{sectionType}`, and `POST /api/pages/{id}/publish`
- Aligned frontend API client with the documented contract:
  - [src/lib/api.ts](../apps/admin-web/src/lib/api.ts)
  - Added shared enum types (`SectionType`, `PageStatus`), stricter `GeneratedSections`, centralized error parsing, and helper methods for read/update/publish flows
- Fixed stricter typing in preview rendering:
  - [src/app/generate/page.tsx](../apps/admin-web/src/app/generate/page.tsx)
- Verified frontend checks again after contract alignment:
  - `npm run lint` → exit 0
  - `npm run build` → exit 0
- Implemented dashboard flow for listing pages by profile ID:
  - [src/app/dashboard/page.tsx](../apps/admin-web/src/app/dashboard/page.tsx)
  - Search by `profileId`, empty/error states, result cards, and direct JSON access for each page
- Added Next.js proxy routes for page reads:
  - [src/app/api/pages/route.ts](../apps/admin-web/src/app/api/pages/route.ts)
  - [src/app/api/pages/[id]/route.ts](../apps/admin-web/src/app/api/pages/%5Bid%5D/route.ts)
- Added dashboard entry point from home page:
  - [src/app/page.tsx](../apps/admin-web/src/app/page.tsx)
- Verified frontend checks after dashboard implementation:
  - `npm run lint` → exit 0
  - `npm run build` → exit 0
- Implemented page detail view by page ID:
  - [src/app/pages/[id]/page.tsx](../apps/admin-web/src/app/pages/%5Bid%5D/page.tsx)
  - Full section rendering, profile/status summary, and review-oriented layout
- Added publish proxy route for admin-web:
  - [src/app/api/pages/[id]/publish/route.ts](../apps/admin-web/src/app/api/pages/%5Bid%5D/publish/route.ts)
- Wired dashboard cards to the real page detail view and kept JSON as a debug link:
  - [src/app/dashboard/page.tsx](../apps/admin-web/src/app/dashboard/page.tsx)
- Verified frontend checks after page detail implementation:
  - `npm run lint` → exit 0
  - `npm run build` → exit 0
- Fixed persisted section deserialization bug in backend repository:
  - [LandingPageRepository.java](../apps/api/src/main/java/com/ai/therapists/api/page/LandingPageRepository.java)
  - Replaced fragile handwritten JSON parsing with `ObjectMapper` for JSONB round-trip safety
- Chose product direction: keep one shared page template and move AI output from free HTML to structured JSON per section
- Added typed backend section models for the official 9-section structure:
  - [StructuredSections.java](../apps/api/src/main/java/com/ai/therapists/api/section_data/StructuredSections.java)
  - [HeaderData.java](../apps/api/src/main/java/com/ai/therapists/api/section_data/HeaderData.java)
  - [HeroData.java](../apps/api/src/main/java/com/ai/therapists/api/section_data/HeroData.java)
  - [AreasOfSupportData.java](../apps/api/src/main/java/com/ai/therapists/api/section_data/AreasOfSupportData.java)
  - [HowIWorkData.java](../apps/api/src/main/java/com/ai/therapists/api/section_data/HowIWorkData.java)
  - [WhatYouCanExpectData.java](../apps/api/src/main/java/com/ai/therapists/api/section_data/WhatYouCanExpectData.java)
  - [SessionFormatsData.java](../apps/api/src/main/java/com/ai/therapists/api/section_data/SessionFormatsData.java)
  - [ContactData.java](../apps/api/src/main/java/com/ai/therapists/api/section_data/ContactData.java)
  - [DisclaimerData.java](../apps/api/src/main/java/com/ai/therapists/api/section_data/DisclaimerData.java)
  - [FooterData.java](../apps/api/src/main/java/com/ai/therapists/api/section_data/FooterData.java)
- Updated OpenAI prompt and backend parsing for structured sections:
  - [PromptAssemblyService.java](../apps/api/src/main/java/com/ai/therapists/api/generation/PromptAssemblyService.java)
  - [AiGenerationService.java](../apps/api/src/main/java/com/ai/therapists/api/generation/AiGenerationService.java)
- Added structured test fixtures and updated backend tests:
  - [StructuredSectionsBuilder.java](../apps/api/src/test/java/com/ai/therapists/api/test/StructuredSectionsBuilder.java)
  - `./mvnw test` → exit 0
- Refactored admin-web to render template-based sections instead of raw HTML strings:
  - [src/components/section-renderers.tsx](../apps/admin-web/src/components/section-renderers.tsx)
  - [src/lib/api.ts](../apps/admin-web/src/lib/api.ts)
  - [src/app/generate/page.tsx](../apps/admin-web/src/app/generate/page.tsx)
  - [src/app/pages/[id]/page.tsx](../apps/admin-web/src/app/pages/%5Bid%5D/page.tsx)
  - [src/app/dashboard/page.tsx](../apps/admin-web/src/app/dashboard/page.tsx)
- Added transition compatibility for legacy drafts:
  - Old HTML-based sections still render via parsing fallback in admin-web
  - No Flyway schema migration required at this stage because `sections` remains JSONB
- Verified frontend again after structured refactor:
  - `npm run build` → exit 0
- Added structured section update proxy route for admin-web:
  - [src/app/api/pages/[id]/sections/[sectionType]/route.ts](../apps/admin-web/src/app/api/pages/%5Bid%5D/sections/%5BsectionType%5D/route.ts)
- Implemented structured editing flow on page detail view:
  - [src/app/pages/[id]/page.tsx](../apps/admin-web/src/app/pages/%5Bid%5D/page.tsx)
  - Per-section editor state, per-section save action, and success/error feedback
- Added reusable structured section editors:
  - [src/components/section-editors.tsx](../apps/admin-web/src/components/section-editors.tsx)
- Verified frontend after structured editing flow:
  - `npm run build` → exit 0

### Next 3 Tasks

1. Return structured section objects directly from backend responses to remove transitional string parsing.
2. Add section-level regeneration endpoint.
3. Add optimistic draft refresh and richer save UX after section updates.

### Current Blocker

No blocker.

### Exact Resume Command

From repo root:
`docker compose -f infra/docker-compose.yml up -d`

From [apps/api](../apps/api):
`./mvnw test`

From [apps/admin-web](../apps/admin-web):
`npm run dev`

---

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
- Admin-web style rationale documented:
  - Objective: position the UI as a calm professional workspace (not a marketing template look).
  - Visual direction: warm neutral palette + soft radial background to reduce clinical coldness while preserving trust.
  - Typography: serif display for structure/authority + geometric sans for forms/readability.
  - Interaction model: split form/preview layout to keep generation workflow explicit and reduce cognitive switching.
  - Backend coupling: proxy route in Next.js to avoid browser CORS friction and keep API base URL configurable per developer.
- Prompt alignment fix after live validation failure (`Forbidden term detected: résultats`):
  - Updated [PromptAssemblyService](../apps/api/src/main/java/com/ai/therapists/api/generation/PromptAssemblyService.java) to include French forbidden vocabulary explicitly in system instructions.

### Next 3 Tasks

1. Add dashboard page listing generated pages by profile ID.
2. Add single page detail view by page ID in admin app.
3. Add publish action from admin page detail view.

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
