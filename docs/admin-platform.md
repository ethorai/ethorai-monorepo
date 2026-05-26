# Admin Platform

Internal admin tooling for Ethorai. Lets the founder (currently sole admin) see all registered therapists, impersonate them to debug/edit pages, create accounts on their behalf after interviews, and configure custom domains.

**Status**: Phase A implemented (2026-05-21). Phase B–D are plans.

---

## Why this exists

Pre-revenue, solo founder, ~5 user interviews lined up. Two recurring needs:

1. **Visibility** — who has registered, what state is their page in, are they actively using it. Currently nothing — only DB queries.
2. **Hands-on assistance** — during/after interviews, therapists who get excited want their page live but may be non-technical. The founder needs to step in: fix content, publish, configure a custom domain.

Both translate to admin tooling. No external admins planned — `is_admin` boolean on `app_user`, flipped manually in DB for the founder's account.

---

## Architectural decision: same Next.js app, not a new frontend

Considered Vue.js as a separate admin SPA. Rejected because:

- Second auth system, second proxy to Spring, duplicated `lib/api.ts` types (~250 lines), duplicated UI components.
- Second Vercel project, second domain, second CI pipeline.
- Solo dev pre-revenue — maintenance cost is real.
- Vue.js learning is a separate hobby project, not coupled to business shipping.

**Decision**: admin lives at `/admin/*` routes inside `apps/admin-web`. Reuses `auth.ts`, `spring-fetch.ts`, `api.ts` types, and the section renderers. Spring side: new `AdminController` with `@PreAuthorize`-style guard on `is_admin`.

---

## Current architecture recap (the parts admin touches)

**Auth**: Auth.js v5 (Next.js) issues a session that contains `springToken` (HS256 JWT signed by Spring). All Next.js proxy routes forward this JWT as `Authorization: Bearer` to Spring. Spring's `JwtAuthenticationFilter` extracts `userId` from the token and puts it in the SecurityContext. `SecurityContextHelper.currentUserId()` reads it.

**Row-level scoping**: every repository method takes `UUID userId` and filters by it. Admin bypasses this — admin endpoints query without user filter.

**Key files admin will plug into**:
- `apps/api/src/main/java/com/ai/therapists/api/security/SecurityConfig.java` — add admin route matchers
- `apps/api/src/main/java/com/ai/therapists/api/security/JwtAuthenticationFilter.java` — already handles JWT; admin uses same flow
- `apps/api/src/main/java/com/ai/therapists/api/security/JwtService.java` — `generateToken(UUID)` for impersonation
- `apps/api/src/main/java/com/ai/therapists/api/profile/TherapistProfileRepository.java` — reference for repo patterns
- `apps/api/src/main/java/com/ai/therapists/api/page/LandingPageRepository.java` — `findLatestByUserId` etc.
- `apps/admin-web/src/auth.ts` — session + springToken flow
- `apps/admin-web/src/lib/spring-fetch.ts` — `withAuth()` pattern for proxy routes
- `apps/admin-web/src/proxy.ts` — middleware (admin routes also need auth gating)

**Migrations are in `apps/api/src/main/resources/db/migration/`**. Next available number is **V9** (V8 added subdomain). After any new migration, regenerate jOOQ: `./mvnw jooq-codegen:generate` then commit the regenerated files in `src/main/generated/jooq/`.

---

## Phased plan

### Phase A — Read-only admin ✅ DONE (2026-05-21)

Goal: founder logs in, navigates to `/admin/users`, sees a table of all registered therapists with their page status.

**What was actually built:**

**Backend (Spring)**:
- [V9__user_is_admin.sql](../apps/api/src/main/resources/db/migration/V9__user_is_admin.sql) — `ALTER TABLE app_user ADD COLUMN is_admin BOOLEAN NOT NULL DEFAULT false`
- [JwtService.java](../apps/api/src/main/java/com/ai/therapists/api/security/JwtService.java) — `generateToken(UUID, boolean isAdmin)` adds `isAdmin` JWT claim; `extractIsAdmin(token)` reads it
- [JwtAuthenticationFilter.java](../apps/api/src/main/java/com/ai/therapists/api/security/JwtAuthenticationFilter.java) — extracts `isAdmin` claim, grants `ROLE_ADMIN` authority when true
- [AuthController.java](../apps/api/src/main/java/com/ai/therapists/api/auth/AuthController.java) — both `/login` and `/oauth` look up `is_admin` from DB before signing JWT
- [AdminController.java](../apps/api/src/main/java/com/ai/therapists/api/admin/AdminController.java) — `GET /api/admin/users` (window-function join: app_user → latest landing_page → therapist_profile); `GET /api/admin/users/{userId}` (user info + full page via existing repos)
- [AdminUserSummary.java](../apps/api/src/main/java/com/ai/therapists/api/admin/AdminUserSummary.java) + [AdminUserDetail.java](../apps/api/src/main/java/com/ai/therapists/api/admin/AdminUserDetail.java) — response records
- [SecurityConfig.java](../apps/api/src/main/java/com/ai/therapists/api/security/SecurityConfig.java) — `.requestMatchers("/api/admin/**").hasRole("ADMIN")`
- [AdminControllerIntegrationTest.java](../apps/api/src/test/java/com/ai/therapists/api/AdminControllerIntegrationTest.java) — 6 tests: 403 for non-admin, 200 for admin on list + detail + 404 on unknown user; 34/34 total suite green

**Frontend (Next.js)**:
- [src/types/next-auth.d.ts](../apps/admin-web/src/types/next-auth.d.ts) — `isAdmin: boolean` added to `Session.user`, `User`, `JWT`
- [src/auth.ts](../apps/admin-web/src/auth.ts) — JWT callback decodes `isAdmin` claim from `springToken` via `decodeIsAdmin()` (base64url payload parse); stored as `token.isAdmin`, exposed in session
- [src/app/api/admin/users/route.ts](../apps/admin-web/src/app/api/admin/users/route.ts) — proxy GET → Spring
- [src/app/api/admin/users/[userId]/route.ts](../apps/admin-web/src/app/api/admin/users/%5BuserId%5D/route.ts) — proxy GET → Spring
- [src/lib/api.ts](../apps/admin-web/src/lib/api.ts) — `AdminUserSummary`, `AdminUserDetail` types; `getAdminUsers()`, `getAdminUserDetail()` client functions
- [src/app/admin/layout.tsx](../apps/admin-web/src/app/admin/layout.tsx) — server component, redirects to `/` if `session.user.isAdmin` is false
- [src/app/admin/users/page.tsx](../apps/admin-web/src/app/admin/users/page.tsx) — server component table: email (link), fullName, subdomain, page status badge, created date
- [src/app/admin/users/[userId]/page.tsx](../apps/admin-web/src/app/admin/users/%5BuserId%5D/page.tsx) — server component: user metadata card + read-only page preview using all 9 section renderers

**Manual seed** (one-time, run in prod DB):
```sql
UPDATE app_user SET is_admin = true WHERE email = 'mednajib.slassi@gmail.com';
```

**Acceptance verified**:
- `./mvnw test` → 34/34 green
- `npm run build` → clean, all admin routes present
- Non-admin hitting `/api/admin/**` → 403
- Admin layout redirects non-admin to `/`

**Estimated effort**: ~3 hours

---

### Phase B — Impersonation

After A, you can see users but can't act as them. Phase B adds:

- `POST /api/admin/impersonate/{userId}` → returns a JWT signed for that userId (with `isAdmin=false` claim — impersonated session has NO admin powers)
- Frontend stores the impersonated JWT in the Auth.js session (separate field like `impersonatedToken` so original admin token is preserved)
- `spring-fetch.ts` `withAuth()` prefers `impersonatedToken` over `springToken` when present
- Persistent banner: "Vous êtes en impersonation de [Nom] · [Quitter l'impersonation]" — clicking clears `impersonatedToken` and returns to admin
- Spring logs an event in `event_log` (`IMPERSONATION_STARTED`, `IMPERSONATION_ENDED`) for audit trail

Critical: impersonation JWTs should be short-lived (15 min) to limit blast radius. Need a refresh mechanism if longer sessions are needed.

**Estimated effort**: ~3 hours

---

### Phase C — Create account on behalf

After A+B, founder can monitor and act as users, but can't create users for them. Phase C adds:

- `POST /api/admin/users` → creates `app_user` + `therapist_profile` + triggers generation, all in one transaction. Returns the new userId + a magic-link login URL for the therapist.
- Magic link: signed JWT in URL, valid 7 days, single-use, lets the user set their password.
- Frontend: `/admin/users/new` — a form that combines all 8 onboarding questions in a single screen (admin doesn't need the Typeform UX).
- Optional: ability to seed a password directly, bypassing magic link.

**Estimated effort**: ~4 hours

---

### Phase D — Custom domain

The user `cabinet-dr-dupont.fr` they bought needs to point to their Ethorai page.

- Migration: `ALTER TABLE therapist_profile ADD COLUMN custom_domain VARCHAR(255) UNIQUE`
- Admin UI to set the domain on a user's profile
- Optional: Vercel API integration to programmatically add the domain (`POST /v10/projects/{id}/domains`). Otherwise manual via Vercel dashboard.
- Middleware `proxy.ts` — if `host` matches a `custom_domain` in DB (cache or per-request lookup), rewrite to `/s/{slug}`. Either:
  - Per-request DB lookup (cached) — needs `is_custom_domain(host)` Spring endpoint exposed publicly
  - Pre-computed env var or static list — simpler but requires redeploy per domain

Coordination overhead with DNS (therapist must add CNAME `@` → `cname.vercel-dns.com`) means this is more ops than code.

**Estimated effort**: ~1 day per domain initially, mostly DNS troubleshooting

---

## Conventions to follow (project-wide)

These are in `CLAUDE.md` but worth restating for admin work:

- **No JPA, no Hibernate, no `@Entity`**. Use jOOQ for all DB queries.
- **MockMvc, not WebTestClient** for tests (we're on Spring MVC, not WebFlux).
- **`@WithMockUser`** on test classes for SecurityContext setup.
- **Spring Boot 4.0** has split auto-configurations — if you add a new technology (e.g., Spring Mail), you may need a separate `spring-boot-X` module (see `spring-boot-flyway` in `pom.xml` as precedent).
- **jOOQ generated sources are committed** at `src/main/generated/jooq/`. After any migration: `./mvnw flyway:migrate && ./mvnw jooq-codegen:generate`, then commit the changes. Never edit these files manually.
- **Commit protocol** (from CLAUDE.md): every commit must also update `docs/progress-tracker.md` and `docs/session-log.md` in the same commit.
- **Tests use delta-based count assertions** — snapshot before, assert `+N` after — so they're rerunnable without DB resets.
- **HTTP error codes**: 400 bad input, 403 permission denied (use this for non-admin hitting `/api/admin/**`), 422 guardrail/validation, 502 AI failure.

---

## Open questions

- **Should admin actions be audited?** Phase A is read-only so no. Phase B (impersonation) and C (account creation) should log to `event_log` — minimal effort, large value if anything goes wrong.
- **Multiple admins later?** Right now `is_admin` is a single boolean. If we ever have multiple admin roles (support vs founder), we'd want a roles table. Defer.
- **Rate limiting on admin endpoints?** Probably overkill. The bucket4j rate limiter we have is per-user — admin endpoints scoped to a single user (the founder) don't really need it.
- **CSV export on the users list?** Useful for tracking interview funnel. Trivial to add to Phase A if needed.

---

## How to use this doc

This doc is BOTH the implementation brief for the next session AND the living spec for the admin platform. When implementing:

1. Read this doc.
2. Read the referenced files (especially `SecurityConfig`, `JwtService`, `auth.ts`, `spring-fetch.ts`).
3. Read `CLAUDE.md` for general conventions.
4. Implement Phase A. Stop.
5. Update this doc as you go — turn the Phase A section into "Phase A — done, here's how it actually got built" with file references. Future phases stay as plans.
