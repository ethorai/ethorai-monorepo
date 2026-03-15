# AI Input Questions — EU Therapist Landing Page (v1)

## Purpose

This document defines the **exact questions** asked to the user before generating a landing page.

The questions are designed to:

- reduce anxiety
- avoid marketing language
- encode ethical and regulatory constraints
- map directly to landing page sections

The AI must not generate content outside the scope defined by these questions.

Maximum number of questions: **7**.

---

## Question 1 — Professional Role

**User-facing question:**  
What is your professional role?

**Examples:**

- Psychologist
- Psychotherapist
- Counselor
- Therapist

**Type:**  
Single select or free text with validation

**Purpose:**

- Determine credibility framing
- Apply role-specific ethical constraints
- Adapt disclaimer logic

**Used in:**

- Header / identity section
- Disclaimer generation
- Guardrail strictness

---

## Question 2 — Who You Work With

**User-facing question:**  
Who do you primarily work with?

**Examples:**

- Adults
- Adolescents
- Couples
- Families

**Type:**  
Multi-select

**Purpose:**

- Help visitors self-identify
- Avoid vague or overly broad messaging

**Used in:**

- Hero section
- Introductory description

---

## Question 3 — Areas of Support

**User-facing question:**  
What are the main topics you work on?

**Examples:**

- Stress and emotional difficulties
- Anxiety-related concerns
- Relationship challenges
- Life transitions

**Type:**  
Multi-select with optional free-text additions

**Constraints:**

- Topics only, no outcomes or results
- No medical diagnoses unless explicitly provided

**Used in:**

- Areas of support section

---

## Question 4 — Therapeutic Approach

**User-facing question:**  
How would you describe your therapeutic approach?

**Examples:**

- Cognitive-behavioral
- Integrative
- Person-centered
- Psychodynamic

**Type:**  
Free text with suggested examples

**Purpose:**

- Professional differentiation
- Establish therapeutic framework

**Used in:**

- “How I work” section

---

## Question 5 — Session Format

**User-facing question:**  
How do you offer sessions?

**Options:**

- In person
- Online
- Both

**Optional follow-up:**

- City / country (for in-person sessions)

**Purpose:**

- Clarify logistics
- Reduce uncertainty for visitors

**Used in:**

- Practical information section
- CTA context

---

## Question 6 — What Clients Can Expect

**User-facing question:**  
What is important for you that clients understand before starting?

**Guided examples:**

- Confidentiality
- Collaborative process
- Respect for personal pace
- Non-judgmental space

**Type:**  
Multi-select with optional short free text

**Purpose:**

- Build trust
- Reassure potential clients

**Used in:**

- “What you can expect” section

---

## Question 7 — Contact / Booking Method

**User-facing question:**  
How should clients contact or book a session?

**Options:**

- Email
- Phone
- External booking link

**Purpose:**

- Provide a clear next step
- Avoid sales-oriented CTAs

**Used in:**

- Call-to-action section
- Footer

---

## Explicitly Excluded Questions

The following questions must NOT be asked in v1:

- “What makes you different from others?”
- “What results do you offer?”
- “Why should clients choose you?”
- “What is your mission or vision?”
- “Describe your ideal client”
- “What outcomes can clients expect?”

These questions increase ethical risk and encourage marketing-oriented output.

---

## Input Contract Guarantee

All AI-generated content must:

- be derivable from the answers to these questions
- respect role-based guardrails
- avoid introducing new claims or concepts

If generation requires information not provided here, the AI must:

- either omit it
- or use neutral, generic phrasing
