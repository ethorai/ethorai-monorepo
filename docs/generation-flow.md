# AI Generation Flow — Therapist Landing Page (v1)

## Purpose

This document defines the **end-to-end generation pipeline** for producing a therapist landing page.

The flow ensures:

- predictable AI behavior
- ethical and regulatory compliance
- reproducible outputs
- safe regeneration when constraints are violated

The system must follow this sequence strictly.

---

## Step 1 — Input Collection

### Inputs

Collected from the therapist via a guided form:

- Full name
- Professional role
- Location (country, city optional)
- Practice format (online / in-person / both)
- Areas of support
- Therapeutic approach (free text)
- Contact or booking method

### Rules

- All inputs are treated as user-provided facts
- No interpretation or enhancement at this stage
- Empty or missing fields are allowed

---

## Step 2 — Input Normalization

### Purpose

Ensure inputs are safe, consistent, and usable.

### Actions

- Trim whitespace
- Normalize capitalization
- Remove emojis and special characters
- Replace forbidden vocabulary if present in inputs (soft correction)
- Convert lists into structured arrays

### Output

A normalized input object ready for generation.

---

## Step 3 — Role Policy Selection

### Purpose

Apply role-specific constraints.

### Actions

- Match selected professional role to a role policy:
  - Psychologist
  - Psychotherapist / Therapist
  - Counselor
- Load corresponding constraint configuration:
  - vocabulary strictness
  - disclaimer template
  - claim limitations

### Output

Active role policy configuration.

---

## Step 4 — Prompt Assembly

### Purpose

Create a deterministic, structured prompt.

### Components

The prompt must include:

- Project statement
- Persona definition
- Landing page structure
- AI guardrails
- Normalized user inputs
- Active role policy

### Rules

- The prompt must be explicit and restrictive
- No open-ended instructions
- No creativity encouragement
- Structure and rules take priority over style

---

## Step 5 — AI Content Generation

### Purpose

Generate the landing page content.

### Actions

- Call the AI model with the assembled prompt
- Request structured output matching section definitions

### Output

Raw generated content (unvalidated).

---

## Step 6 — Validation

### Purpose

Ensure compliance before rendering.

### Validation Checks

The system must verify:

- all required sections are present
- section order is respected
- forbidden vocabulary is absent
- tone and claims follow guardrails
- role-based constraints are respected
- disclaimer is present and correct
- structural limits are respected

### Outcome

- If validation passes → proceed
- If validation fails → trigger regeneration

---

## Step 7 — Regeneration Loop

### Purpose

Correct violations automatically.

### Actions

- Identify failed validation rules
- Regenerate only the failing sections if possible
- Apply stricter constraints if repeated failures occur

### Limits

- Maximum 3 regeneration attempts
- After 3 failures, return an error state

---

## Step 8 — Output Formatting

### Purpose

Prepare content for rendering.

### Actions

- Convert structured content into UI-ready format
- Preserve section boundaries
- Ensure consistent spacing and typography

### Output

Final landing page content ready for display or export.

---

## Step 9 — Logging & Explainability (Internal)

### Purpose

Support maintainability and trust.

### Logged Data

- Active role policy
- Guardrails applied
- Validation results
- Regeneration triggers (if any)

This data is internal and not exposed to end users.

---

## Admin HTTP Contract (Current)

This is the current contract used by `apps/admin-web` to communicate with `apps/api`.

### 1. Generate Page Draft

`POST /api/generate`

Request body:

```json
{
  "fullName": "Najib Slassi",
  "role": "THERAPIST",
  "location": "Paris",
  "audiences": ["Adults"],
  "areasOfSupport": ["Stress", "Anxiety"],
  "approach": "Integrative",
  "sessionFormat": "ONLINE",
  "expectations": ["Confidentiality", "Respect"],
  "contactMethod": "EMAIL",
  "contactValue": "najib@email.com"
}
```

Success response: `201 Created`

Headers:

- `Location: /api/pages/{pageId}`

Response body:

```json
{
  "pageId": "uuid",
  "profileId": "uuid",
  "fullName": "Najib Slassi",
  "role": "THERAPIST",
  "sections": {
    "HEADER": "{\"name\":\"Najib Slassi\",\"role\":\"THERAPIST\",\"location\":\"Paris\",\"phone\":null,\"email\":\"najib@email.com\"}",
    "HERO": "{\"heading\":\"Je travaille avec les adultes\",\"subheading\":\"Un accompagnement psychologique dans un cadre respectueux et confidentiel.\"}",
    "AREAS_OF_SUPPORT": "{\"title\":\"Domaines d'accompagnement\",\"items\":[\"Stress\",\"Anxiété\"]}",
    "HOW_I_WORK": "{\"title\":\"Ma pratique\",\"description\":\"J'adopte une approche intégrative et collaborative.\"}",
    "WHAT_YOU_CAN_EXPECT": "{\"title\":\"Ce que vous pouvez attendre\",\"statements\":[\"Un espace confidentiel\",\"Une écoute respectueuse\"]}",
    "SESSION_FORMATS": "{\"title\":\"Formats de séances\",\"formats\":[{\"type\":\"ONLINE\",\"details\":\"Séances en ligne\"}]}",
    "CONTACT": "{\"title\":\"Contact\",\"description\":\"Contactez-moi pour une première séance.\",\"cta_text\":\"Prendre rendez-vous\",\"phone\":null,\"email\":\"najib@email.com\"}",
    "DISCLAIMER": "{\"text\":\"Les séances ne se substituent pas à un avis médical.\"}",
    "FOOTER": "{\"name\":\"Najib Slassi\",\"role\":\"THERAPIST\",\"location\":\"Paris\",\"phone\":null,\"email\":\"najib@email.com\"}"
  },
  "status": "DRAFT"
}
```

Notes:

- The AI now generates structured JSON per section, not free HTML.
- The backend currently stores each section as a JSON string inside the `sections` map.
- `apps/admin-web` parses those strings and renders them through shared templates.
- Returning structured section objects directly is still a planned follow-up.

Error responses:

- `400 Bad Request` for invalid input payload
- `422 Unprocessable Entity` for guardrail/validation failure
- `502 Bad Gateway` for AI generation failure

### 2. Get Single Generated Page

`GET /api/pages/{id}`

Success response: `200 OK`

Response body: same shape as `POST /api/generate` response.

Error responses:

- `404 Not Found` if page does not exist

### 3. List Pages For One Profile

`GET /api/pages?profileId={uuid}`

Success response: `200 OK`

Response body:

```json
[
  {
    "pageId": "uuid",
    "profileId": "uuid",
    "fullName": "Najib Slassi",
    "role": "THERAPIST",
    "sections": {
      "HEADER": "{\"name\":\"Najib Slassi\",\"role\":\"THERAPIST\",\"location\":\"Paris\",\"phone\":null,\"email\":\"najib@email.com\"}"
    },
    "status": "DRAFT"
  }
]
```

Error responses:

- `404 Not Found` if the profile does not exist

### 4. Update One Section

`PUT /api/pages/{id}/sections/{sectionType}`

Request body:

```text
{"title":"Ma pratique","description":"Approche intégrative et collaborative."}
```

Notes:

- The current endpoint still accepts plain text at HTTP level.
- With structured sections, the intended payload is a JSON string matching the target section shape.

Success response: `204 No Content`

Error responses:

- `404 Not Found` if page does not exist

### 5. Publish Draft

`POST /api/pages/{id}/publish`

Success response: `204 No Content`

Error responses:

- `404 Not Found` if page does not exist

### Shared Enums

Role values:

- `PSYCHOLOGIST`
- `THERAPIST`
- `COUNSELOR`

Session format values:

- `ONLINE`
- `IN_PERSON`
- `BOTH`

Contact method values:

- `EMAIL`
- `PHONE`
- `BOOKING_LINK`

Page status values:

- `DRAFT`
- `PUBLISHED`

Section keys:

- `HEADER`
- `HERO`
- `AREAS_OF_SUPPORT`
- `HOW_I_WORK`
- `WHAT_YOU_CAN_EXPECT`
- `SESSION_FORMATS`
- `CONTACT`
- `DISCLAIMER`
- `FOOTER`
