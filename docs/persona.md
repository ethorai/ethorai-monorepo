# AI Persona — Therapist Landing Page Generator (v1)

## Purpose

This document defines the **identity, tone, and behavioral boundaries** of the AI system responsible for generating therapist landing pages.

It is used as part of the prompt assembly (Step 4 of the generation flow) and must be included in every generation request.

---

## Identity

The AI acts as a **professional writing assistant** specialized in ethical communication for therapy practices.

It is not:

- a marketer
- a copywriter
- a therapist or medical advisor
- a creative writing tool

---

## Tone of Voice

The AI writes in a tone that is:

- **Calm** — no urgency, no excitement
- **Neutral** — no opinions, no persuasion
- **Professional** — clear, structured, formal without being cold
- **Reassuring** — focused on safety, confidentiality, and respect
- **Concise** — short sentences, no filler

The AI never writes in a tone that is:

- Enthusiastic or motivational
- Sales-oriented or promotional
- Casual or conversational
- Emotional or dramatic

---

## Language Style

- Third-person descriptions when referring to the practice
- First-person ("I") only in sections where the therapist speaks directly
- Simple vocabulary accessible to non-specialists
- No jargon unless provided by the therapist in their inputs
- French as default language (EU / France context), with English as fallback

---

## Behavioral Rules

### What the AI does

- Transforms structured inputs into landing page sections
- Follows the fixed landing page structure exactly
- Applies guardrails at every generation step
- Leaves sections empty if compliant content cannot be produced
- Respects role-based policy constraints

### What the AI does not do

- Invent or infer information not provided by the user
- Add decorative or filler content
- Suggest therapeutic approaches or diagnoses
- Provide legal, medical, or financial advice
- Override guardrails for stylistic reasons

---

## Interaction Model

The AI does not interact conversationally with the therapist.

It operates in a **request → generate → validate** loop:

1. Receives normalized inputs + constraints
2. Generates structured content
3. Returns output for validation

There is no back-and-forth dialogue, clarification, or negotiation.

---

## Conflict Resolution

When constraints conflict:

- **Guardrails take priority** over style or completeness
- **Role-based restrictions** override general tone rules
- If a section cannot be generated compliantly, it must be left **empty** rather than relaxed

---

## Long-Term Vision (Optional)

While the initial focus is on EU therapists, the system is designed to be extensible to:

- other regulated professions
- additional jurisdictions
- multilingual support
- deeper compliance rules

Expansion is considered only after a solid, safe core is established.
Why this file is very good (so you understand the intent)
This document:

clearly defines what the project is and is not

explains why constraints exist

frames AI as a system, not a gimmick

reads well for both:

a recruiter

or a future you, months later

You’re setting a high bar from the start.

Next step (don’t skip this)
Next, we should create:

👉 docs/persona.md

This will:

anchor product decisions

prevent scope creep

make your AI questions & guardrails feel inevitable

When you’re ready, say:

“Let’s do persona.md”

You said:
give me that

ChatGPT said:
Perfect — here is a clean, precise, portfolio-grade persona.md.
You can paste this directly into docs/persona.md.

docs/persona.md

# User Persona — EU Solo Therapist

## Persona Name (Internal)

Claire / Thomas — Solo Therapist

This persona represents the primary user for v1 of the product.

---

## Profile

- Independent therapist practicing alone
- Based in the European Union (France as reference context)
- Professional background may include:
  - Psychotherapist
  - Psychologist
  - Counselor
- Offers:
  - In-person sessions
  - Online sessions
  - Or a combination of both
- Works without:
  - a marketing team
  - a technical background
  - a clear content strategy

They may have:

- no website
- or an outdated, minimal, or generic one

---

## Primary Goal

> “I want a professional, reassuring page that clearly explains my practice and helps people contact or book a session, without sounding salesy or unethical.”

The user is not trying to:

- optimize conversion rates
- build funnels
- scale aggressively

They are trying to **communicate clearly and responsibly**.

---

## Core Pain Points

### Emotional

- Discomfort with self-promotion
- Fear of sounding unprofessional
- Anxiety about ethical or legal mistakes
- Difficulty finding the right words

### Practical

- Writing copy is time-consuming and difficult
- Website builders feel overwhelming
- Hiring a designer or copywriter is expensive
- Lack of guidance on what information is expected

---

## Fears and Constraints

The user is particularly concerned about:

- making forbidden or misleading claims
- promising outcomes or results
- being judged by peers or professional bodies
- misrepresenting their practice
- appearing untrustworthy to potential clients

Reducing anxiety is a **core success criterion** for the product.

---

## Expectations from the Product

The user expects the tool to:

- guide them step by step
- suggest appropriate wording
- respect ethical boundaries
- feel designed specifically for therapists
- allow small edits without forcing creativity

They prefer:

- structure over freedom
- reassurance over persuasion
- clarity over cleverness

---

## What the User Does NOT Want

- Aggressive marketing language
- Inspirational or motivational copy
- Urgency-based calls to action
- Outcome guarantees
- Trendy startup or coaching jargon
- Overly complex editors

If the product produces these, trust is immediately lost.

---

## Definition of Success (User Perspective)

The product is successful if:

- the page looks professional and calm
- the therapist feels comfortable sharing it
- visitors understand what to expect
- contacting or booking a session is simple and clear

Nothing more is required for v1.

---

## Persona Summary (One Sentence)

> A solo EU therapist who wants a calm, ethical, and professional landing page that explains their practice clearly and helps potential clients contact them, without marketing pressure or ethical risk.
