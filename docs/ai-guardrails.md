# AI Guardrails — EU Therapist Landing Page Generator (v1)

## Purpose

This document defines the **mandatory constraints** applied to all AI-generated content in the system.

The goal of these guardrails is to:

- ensure ethical and professional communication
- reduce legal and reputational risk
- prevent sales-oriented or outcome-based language
- maintain trust for therapists and potential clients
- make AI behavior predictable and explainable

These rules are enforced programmatically and must be respected at generation and regeneration time.

---

## 1. Tone Guardrails

### Required Tone

All generated content must be:

- Calm
- Neutral
- Professional
- Respectful
- Reassuring

### Forbidden Tones

The AI must not generate content that is:

- Sales-oriented
- Inspirational or motivational
- Urgent or time-pressured
- Emotional or manipulative
- Casual or promotional

Tone violations require regeneration.

---

## 2. Vocabulary Guardrails

### Forbidden Terms and Expressions

The following words or phrases must not appear in generated content.  
Their presence must trigger regeneration or blocking.

- heal / healing
- cure / cured
- fix / fixed
- eliminate
- guarantee / guaranteed
- results
- transformation / transformative
- breakthrough
- proven
- best / top / expert
- life-changing
- struggle (use “difficulty” or “challenge” instead)

This list is non-exhaustive and may be extended.

---

### Allowed Vocabulary Patterns

Preferred wording includes:

- “support”
- “work with”
- “accompany”
- “explore”
- “provide a space”
- “collaborative”
- “adapted to each person”

---

## 3. Claims Guardrails

### Forbidden Claims

The AI must not:

- promise or imply outcomes
- guarantee improvement or success
- imply medical or therapeutic results
- suggest speed or certainty
- replace professional or medical advice

---

### Allowed Claims

Claims must be:

- Descriptive
- Process-based
- Conditional

**Allowed example:**

> “Sessions focus on understanding and working through difficulties.”

**Forbidden example:**

> “Sessions will help you overcome anxiety.”

---

## 4. Structural Guardrails

### Global Structure Rules

- Maximum 2 sentences per paragraph
- Average sentence length: 12–20 words
- No emojis
- No rhetorical questions
- No exclamation points

---

### Section-Specific Rules

- Hero headline: maximum 12 words
- Bullet points: descriptive, noun-based
- Call-to-action text: 3–4 words maximum

Structural violations require regeneration.

---

## 5. Role-Based Guardrails

The strictness of constraints depends on the professional role selected.

### Psychologist

- Strongest restrictions
- Mandatory ethical disclaimer
- Avoid naming mental health conditions unless explicitly provided

### Psychotherapist / Therapist

- Moderate restrictions
- Process-oriented explanations

### Counselor

- Slightly more flexible phrasing
- Still no outcome-based claims

Role-based rules must be applied consistently.

---

## 6. Ethical Framing Guardrails

The AI must:

- emphasize confidentiality
- respect client autonomy
- avoid dependency or authority framing

### Forbidden Framing

- “I will guide you”
- “I will lead you”
- “You need help”
- “I know what is best for you”

### Allowed Framing

- “We work together”
- “Sessions are collaborative”
- “At your own pace”
- “In a respectful and confidential setting”

---

## 7. Disclaimer Guardrails

### Mandatory Disclaimer

Every generated landing page must include an ethical disclaimer:

- adapted to the professional role
- written in a calm, non-alarmist tone
- avoiding legal or medical advice

If a disclaimer is missing, generation must fail.

---

## 8. Regeneration Triggers

The system must trigger regeneration if:

- forbidden vocabulary appears
- outcome-based or guaranteed claims are detected
- tone deviates from neutral/professional
- structural limits are exceeded
- the disclaimer is missing or incorrect
- role-based rules are violated

---

## 9. Explainability Requirement

The system should be able to explain internally:

- which guardrails were applied
- why content was regenerated or blocked

This supports maintainability, debugging, and responsible AI design.
