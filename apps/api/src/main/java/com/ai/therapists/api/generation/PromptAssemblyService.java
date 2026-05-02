package com.ai.therapists.api.generation;

import com.ai.therapists.api.page.SectionType;
import com.ai.therapists.api.profile.TherapistInput;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class PromptAssemblyService {

    public String assembleSystemPrompt(TherapistInput input) {
        return """
                You are a professional writing assistant specialized in ethical communication for therapy practices.

                ## Identity
                - You are NOT a marketer, copywriter, therapist, or medical advisor.
                - You transform structured inputs into landing page sections.

                ## Tone
                - Calm, neutral, professional, respectful, reassuring, concise.
                - NEVER: sales-oriented, inspirational, urgent, emotional, casual, promotional.

                ## Vocabulary Rules
                FORBIDDEN words (English): heal, healing, cure, cured, fix, fixed, eliminate, guarantee, guaranteed, \
                results, transformation, transformative, breakthrough, proven, best, top, expert, life-changing, struggle.
                FORBIDDEN words (French): guérir, guérison, éliminer, garantie, résultats, transformation, \
                percée, prouvé, meilleur, expert, lutte.
                USE INSTEAD: accompagner, travailler ensemble, explorer, offrir un espace, collaboratif, adapté à chaque personne.

                ## Claims Rules
                - Never promise or imply outcomes.
                - Never guarantee improvement or success.
                - Never suggest speed or certainty.

                ## Structural Rules
                - Maximum 2 sentences per paragraph.
                - Average sentence length: 12–20 words.
                - No emojis, no rhetorical questions, no exclamation points.
                - Hero headline: maximum 12 words.
                - Bullet points: descriptive, noun-based.
                - Call-to-action text: 3–4 words maximum.

                ## Role-Based Policy (%s)
                %s

                ## Ethical Framing
                - Emphasize confidentiality and respect client autonomy.
                - FORBIDDEN: "I will guide you", "I will lead you", "You need help", "I know what is best for you".
                - ALLOWED: "We work together", "Sessions are collaborative", "At your own pace", "In a respectful and confidential setting".

                ## Disclaimer
                Every page MUST include an ethical disclaimer adapted to the role. If it cannot be produced compliantly, leave it empty.

                ## Language
                French by default (EU / France context). Use simple vocabulary accessible to non-specialists.
                Use first-person ("Je") in sections where the therapist speaks directly.
                """.formatted(input.role().name(), rolePolicy(input));
    }

    public String assembleUserPrompt(TherapistInput input) {
        return """
                Generate a structured landing page for the therapist below. Return a JSON object with the exact structure below.

                ## Therapist Information
                - Full Name: %s
                - Role: %s
                - Location: %s
                - Target Audiences: %s
                - Areas of Support: %s
                - Approach: %s
                - Session Format: %s
                - What Clients Can Expect: %s
                - Phone: %s
                - Email: %s
                - Booking Link: %s

                ## JSON Structure (return ONLY this object, no markdown, no explanation):

                {
                  "HEADER": {
                    "name": "therapist full name",
                    "role": "professional role",
                    "location": "city/country",
                    "phone": "phone or null",
                    "email": "email or null"
                  },
                  "HERO": {
                    "heading": "headline (max 12 words)",
                    "subheading": "explanation of who practice is for"
                  },
                  "AREAS_OF_SUPPORT": {
                    "title": "section title",
                    "items": [
                      {"title": "concise topic title (3-6 words)", "description": "1-2 sentences of professional, neutral description; topic-focused, no outcome promises"},
                      ...
                    ]
                  },
                  "HOW_I_WORK": {
                    "title": "section title",
                    "description": "one paragraph describing the therapeutic approach"
                  },
                  "WHAT_YOU_CAN_EXPECT": {
                    "title": "section title",
                    "statements": [
                      {"title": "short value or principle (3-6 words)", "description": "1-2 sentences elaborating on this principle; calm professional tone, environment-focused"},
                      ...
                    ]
                  },
                  "SESSION_FORMATS": {
                    "title": "section title",
                    "formats": [
                      {"type": "human-friendly label like 'En cabinet' or 'En visio'", "details": "1-2 sentences with practical info: location for in-person, platform for online, duration if relevant"}
                    ]
                  },
                  "CONTACT": {
                    "title": "section title",
                    "description": "description of contact/booking",
                    "cta_text": "3-4 word call to action",
                    "phone": "phone or null",
                    "email": "email or null",
                    "booking_link": "booking URL or null"
                  },
                  "DISCLAIMER": {
                    "text": "ethical disclaimer adapted to role"
                  },
                  "FOOTER": {
                    "name": "therapist full name",
                    "role": "professional role",
                    "location": "city/country",
                    "phone": "phone or null",
                    "email": "email or null"
                  }
                }

                ## Rules for Each Field
                - All strings must be in French
                - All null values should be JSON null (not empty strings)
                - HERO heading: maximum 12 words
                - AREAS_OF_SUPPORT items: 3-7 entries, each with title (3-6 words) + description (1-2 sentences); neutral & descriptive, topic-focused, never frame items as problems to be solved
                - HOW_I_WORK description: 1-2 sentences, process-oriented
                - WHAT_YOU_CAN_EXPECT statements: 3-5 entries, each with title (3-6 words) + description (1-2 sentences); about the therapeutic environment & values, not outcomes
                - SESSION_FORMATS formats: 1-2 entries; type MUST be a human-friendly French label (e.g., "En cabinet", "En visio", "Les deux") — never the raw enum (ONLINE/IN_PERSON/BOTH); details: 1-2 sentences with practical info
                - CONTACT: copy the provided phone/email/booking_link values exactly; set fields to null if not provided; cta_text: 3-4 words, neutral CTA
                - DISCLAIMER text: mandatory, calm tone, no legal advice

                Return ONLY the JSON object.
                """.formatted(
                input.fullName(),
                input.role().name(),
                input.location() != null ? input.location() : "Non spécifié",
                String.join(", ", input.audiences()),
                String.join(", ", input.areasOfSupport()),
                input.approach() != null ? input.approach() : "Non spécifié",
                input.sessionFormat().name(),
                input.expectations() != null ? String.join(", ", input.expectations()) : "Non spécifié",
                input.phone() != null ? input.phone() : "null",
                input.email() != null ? input.email() : "null",
                input.bookingLink() != null ? input.bookingLink() : "null"
        );
    }

    private String sectionKeys() {
        return Arrays.stream(SectionType.values())
                .map(SectionType::name)
                .collect(Collectors.joining(", "));
    }

    public String assembleSectionRegenerationPrompt(TherapistInput input, SectionType sectionType, String currentContent) {
        String sectionSchema = sectionJsonSchema(sectionType);
        return """
                Regenerate ONLY the %s section of a therapist landing page.

                ## Therapist Information
                - Full Name: %s
                - Role: %s
                - Location: %s
                - Target Audiences: %s
                - Areas of Support: %s
                - Approach: %s
                - Session Format: %s
                - What Clients Can Expect: %s
                - Phone: %s
                - Email: %s
                - Booking Link: %s

                ## Current Content of This Section
                %s

                ## Expected JSON Structure (return ONLY this object, no markdown, no explanation):
                %s

                ## Rules
                - All strings must be in French.
                - Produce a meaningfully different variation, not a copy of the current content.
                - Follow all tone, vocabulary, and structural rules from the system prompt.
                - Return ONLY the JSON object for this single section.
                """.formatted(
                sectionType.name(),
                input.fullName(),
                input.role().name(),
                input.location() != null ? input.location() : "Non spécifié",
                String.join(", ", input.audiences()),
                String.join(", ", input.areasOfSupport()),
                input.approach() != null ? input.approach() : "Non spécifié",
                input.sessionFormat().name(),
                input.expectations() != null ? String.join(", ", input.expectations()) : "Non spécifié",
                input.phone() != null ? input.phone() : "null",
                input.email() != null ? input.email() : "null",
                input.bookingLink() != null ? input.bookingLink() : "null",
                currentContent,
                sectionSchema
        );
    }

    private String sectionJsonSchema(SectionType sectionType) {
        return switch (sectionType) {
            case HEADER -> """
                    { "name": "...", "role": "...", "location": "...", "phone": "... or null", "email": "... or null" }""";
            case HERO -> """
                    { "heading": "headline (max 12 words)", "subheading": "..." }""";
            case AREAS_OF_SUPPORT -> """
                    { "title": "...", "items": [{"title": "topic (3-6 words)", "description": "1-2 sentences"}, ...] }""";
            case HOW_I_WORK -> """
                    { "title": "...", "description": "..." }""";
            case WHAT_YOU_CAN_EXPECT -> """
                    { "title": "...", "statements": [{"title": "principle (3-6 words)", "description": "1-2 sentences"}, ...] }""";
            case SESSION_FORMATS -> """
                    { "title": "...", "formats": [{"type": "human label e.g. 'En cabinet' or 'En visio'", "details": "1-2 sentences"}, ...] }""";
            case CONTACT -> """
                    { "title": "...", "description": "...", "cta_text": "3-4 words", "phone": "... or null", "email": "... or null", "booking_link": "... or null" }""";
            case DISCLAIMER -> """
                    { "text": "ethical disclaimer" }""";
            case FOOTER -> """
                    { "name": "...", "role": "...", "location": "...", "phone": "... or null", "email": "... or null" }""";
        };
    }

    private String rolePolicy(TherapistInput input) {
        return switch (input.role()) {
            case PSYCHOLOGIST -> """
                    Strongest restrictions. Mandatory ethical disclaimer. \
                    Avoid naming mental health conditions unless explicitly provided by the therapist.""";
            case THERAPIST -> """
                    Moderate restrictions. Process-oriented explanations. \
                    Must include ethical disclaimer.""";
            case COUNSELOR -> """
                    Slightly more flexible phrasing. Still no outcome-based claims. \
                    Must include ethical disclaimer.""";
        };
    }
}
