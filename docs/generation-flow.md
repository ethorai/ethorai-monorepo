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
