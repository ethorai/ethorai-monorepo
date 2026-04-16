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
Stored and queried via **jOOQ** (no JPA/Hibernate).

```java
public record TherapistProfile(
    UUID id,
    String fullName,
    RoleType role,
    String location,
    List<String> audiences,        // stored as JSONB
    List<String> areasOfSupport,   // stored as JSONB
    String approach,
    SessionFormat sessionFormat,
    List<String> expectations,     // stored as JSONB
    ContactMethod contactMethod,
    String contactValue,
    Instant createdAt
) {}
```

---

## 4. Domain Model — LandingPage

Persisted entity representing a generated landing page.  
Stored and queried via **jOOQ** (no JPA/Hibernate).

```java
public record LandingPage(
    UUID id,
    UUID profileId,
    UUID userId,
    StructuredSections sections,  // stored as JSONB, deserialized to typed section objects
    PageStatus status,
    GenerationLog generationLog,  // stored as JSONB
    Instant createdAt,
    Instant updatedAt
) {}
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

Each section has a dedicated typed Java record (e.g. `HeroData`, `HeaderData`).  
The full page is represented by `StructuredSections`, a record aggregating all 9 typed section objects.  
Stored as a single JSONB column in PostgreSQL; deserialized via `ObjectMapper` on read.

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
    StructuredSections sections,
    PageStatus status
)
```

The API returns fully typed structured section objects directly.  
Each section field in `StructuredSections` corresponds to a dedicated typed record (e.g. `HeroData`, `ContactData`).

---

## Relationships

```
TherapistProfile 1 ──── * LandingPage
                           └── sections (StructuredSections — typed, stored as JSONB)
                           └── generationLog (GenerationLog — stored as JSONB)

EventLog (append-only, references any entity by type + id)
```

A therapist can regenerate their page multiple times.  
Each generation creates a new `LandingPage` record (history is preserved).
