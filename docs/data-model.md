# Data Model — AI Therapists (v1)

## Purpose

This document defines the core data structures used across the system:  
DTOs (API layer), domain models (persistence), and internal types (generation pipeline).

---

## 1. Input DTO — TherapistInput

Represents the raw form submission from the therapist (7 questions).

```java
public record TherapistInput(
    @NotBlank String fullName,
    @NotNull RoleType role,
    String location,                    // nullable
    @NotEmpty List<String> audiences,   // e.g. ["Adults", "Couples"]
    @NotEmpty List<String> areasOfSupport,
    String approach,                    // free text, nullable
    @NotNull SessionFormat sessionFormat,
    List<String> expectations,          // e.g. ["Confidentiality", "Non-judgmental space"]
    @NotNull ContactMethod contactMethod,
    @NotBlank String contactValue       // email, phone, or URL
)
```

---

## 2. Enums

### RoleType

```java
public enum RoleType {
    PSYCHOLOGIST,
    THERAPIST,
    COUNSELOR
}
```

Drives role-based guardrail strictness (see ai-guardrails.md §5).

### SessionFormat

```java
public enum SessionFormat {
    ONLINE,
    IN_PERSON,
    BOTH
}
```

### ContactMethod

```java
public enum ContactMethod {
    EMAIL,
    PHONE,
    BOOKING_LINK
}
```

---

## 3. Domain Model — TherapistProfile

Persisted entity representing the therapist's data after normalization.

```java
@Entity
public class TherapistProfile {
    @Id UUID id;
    String fullName;
    @Enumerated RoleType role;
    String location;
    List<String> audiences;        // stored as JSON
    List<String> areasOfSupport;   // stored as JSON
    String approach;
    @Enumerated SessionFormat sessionFormat;
    List<String> expectations;     // stored as JSON
    @Enumerated ContactMethod contactMethod;
    String contactValue;
    Instant createdAt;
}
```

---

## 4. Domain Model — LandingPage

Persisted entity representing a generated landing page.

```java
@Entity
public class LandingPage {
    @Id UUID id;
    @ManyToOne TherapistProfile profile;
    Map<SectionType, String> sections;  // stored as JSONB, each value is a serialized structured section object
    @Enumerated PageStatus status;
    GenerationLog generationLog;        // stored as JSONB
    Instant createdAt;
    Instant updatedAt;
}
```

### SectionType

```java
public enum SectionType {
    HEADER,
    HERO,
    AREAS_OF_SUPPORT,
    HOW_I_WORK,
    WHAT_YOU_CAN_EXPECT,
    SESSION_FORMATS,
    CONTACT,
    DISCLAIMER,
    FOOTER
}
```

Maps 1:1 to the landing page structure (see landing-page-structure.md).

Current storage convention:

- The map key is the section identifier (`HEADER`, `HERO`, etc.)
- The map value is a JSON string representing the structured payload of that section
- Example: `sections.get(SectionType.HERO)` stores something like `{ "heading": "...", "subheading": "..." }`

### PageStatus

```java
public enum PageStatus {
    DRAFT,
    PUBLISHED
}
```

---

## 5. Internal Type — GenerationLog

Embedded in LandingPage. Not exposed via API. Used for debugging and explainability.

```java
public record GenerationLog(
    RoleType rolePolicy,
    List<String> guardrailsApplied,
    int generationAttempts,
    List<String> validationFailures,    // empty if first attempt passed
    Instant generatedAt
)
```

---

## 6. Event Log — EventLog

Append-only table for tracking key system events. Not exposed via API.

```java
public record EventLog(
    Long id,                    // BIGSERIAL, auto-increment
    EntityType entityType,      // PROFILE / PAGE
    UUID entityId,
    EventType eventType,
    Map<String, Object> payload, // nullable, free-form details
    Instant createdAt
)
```

### EntityType

```java
public enum EntityType {
    PROFILE,
    PAGE
}
```

### EventType

```java
public enum EventType {
    CREATED,
    UPDATED,
    GENERATED,
    PUBLISHED,
    GENERATION_FAILED
}
```

---

## 7. Output DTO — GeneratedPageResponse

Returned to the frontend after generation.

```java
public record GeneratedPageResponse(
    UUID pageId,
    UUID profileId,
    String fullName,
    RoleType role,
    Map<SectionType, String> sections,
    PageStatus status
)
```

Notes:

- The AI now generates structured section objects, not raw HTML fragments.
- The current API response still exposes `sections` as `Map<SectionType, String>` for transition compatibility.
- Each string value is serialized JSON for the corresponding structured section.
- A later cleanup can expose fully typed section objects directly in the API.

---

## Relationships

```
TherapistProfile 1 ──── * LandingPage
                           └── sections (Map<SectionType, String serialized JSON>)
                           └── generationLog (GenerationLog)

EventLog (append-only, references any entity by type + id)
```

A therapist can regenerate their page multiple times.  
Each generation creates a new `LandingPage` record (history is preserved).
