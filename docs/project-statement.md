# Project Statement — AI Therapists Landing Page Generator

## Overview

This project is an AI-assisted SaaS designed to help **solo therapists in the European Union** create professional, ethical, and trustworthy landing pages for their practice.

The system focuses on **clarity, trust, and compliance**, rather than marketing optimization or aggressive conversion tactics.  
It is built as an AI-native product with explicit constraints, guardrails, and explainable generation logic.

---

## Problem

Many solo therapists:

- struggle to write professional and ethical website copy
- feel uncomfortable with marketing language
- fear making incorrect or unethical claims
- rely on outdated websites or generic page builders not adapted to their profession

Existing AI website generators often:

- use sales-oriented language
- promise outcomes or results
- ignore regulatory and ethical constraints
- prioritize speed over responsibility

This creates a gap for a **calm, guided, and profession-aware solution**.

---

## Solution

This project provides:

- a **guided AI flow** that asks therapists structured, non-marketing questions
- a **fixed landing page structure** adapted to therapy practices
- **EU-oriented ethical and linguistic guardrails**
- automatic generation of clear, neutral, and reassuring copy
- support for both **online and in-person sessions**

The AI does not replace professional judgment.  
It assists therapists in expressing their practice clearly and safely.

---

## Target Users

- Independent / solo therapists
- Psychotherapists, counselors, and psychologists
- Practicing in the EU (France as reference context)
- Offering online and/or in-person sessions
- With limited technical or marketing expertise

---

## Design Principles

- Ethics before conversion
- Constraints over creativity
- Process-based explanations, not outcome promises
- Calm and professional tone
- Explicit guardrails enforced at generation time

---

## Scope

### MVP (v0.1) — Ship first

- Guided form (7 questions)
- Single role: Psychologist only
- AI generation (single call, all sections)
- Basic vocabulary guardrail (forbidden words check)
- Generated page viewable via unique URL
- Ethical disclaimer (hardcoded template)
- No auth, no accounts
- French language only

### v1 — Full guardrails

- All 3 roles (Psychologist, Therapist, Counselor) with role-based policies
- Full guardrail pipeline (tone, vocabulary, claims, structure)
- Automated validation + regeneration loop (max 3 attempts)
- Section-level regeneration
- Generation log and explainability
- Page status (draft / published)

### Explicitly excluded (all versions for now)

- Funnels or upselling logic
- Testimonials or success stories
- Outcome-based claims
- Medical diagnosis or advice
- Complex CMS or drag-and-drop editors
- User authentication / accounts (v2)
- Custom domains (v2)
- Payment / billing (v2)

---

## Technical Intent

The project is intentionally designed to demonstrate:

- AI orchestration beyond simple prompting
- Guardrail-based generation
- Role-aware policy enforcement
- Maintainable system architecture
- Responsible AI design in a regulated context

This makes the project suitable both as:

- a real-world SaaS foundation
- and a portfolio project demonstrating senior-level engineering and product thinking.

---

## Long-Term Vision (Optional)

While the initial focus is on EU therapists, the system is designed to be extensible to:

- other regulated professions
- additional jurisdictions
- multilingual support
- deeper compliance rules

Expansion is considered only after a solid, safe core is established.
