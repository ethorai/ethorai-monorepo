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
                - Contact Method: %s
                - Contact Value: %s

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
                    "items": ["topic1", "topic2", "topic3", ...]
                  },
                  "HOW_I_WORK": {
                    "title": "section title",
                    "description": "one paragraph describing the therapeutic approach"
                  },
                  "WHAT_YOU_CAN_EXPECT": {
                    "title": "section title",
                    "statements": ["statement1", "statement2", "statement3", ...]
                  },
                  "SESSION_FORMATS": {
                    "title": "section title",
                    "formats": [
                      {"type": "ONLINE", "details": "details about online sessions"},
                      {"type": "IN_PERSON", "details": "details about in-person sessions"}
                    ]
                  },
                  "CONTACT": {
                    "title": "section title",
                    "description": "description of contact/booking",
                    "cta_text": "3-4 word call to action",
                    "phone": "phone or null",
                    "email": "email or null"
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
                - AREAS_OF_SUPPORT items: 3-7 topics, neutral & descriptive
                - HOW_I_WORK description: 1-2 sentences, process-oriented
                - WHAT_YOU_CAN_EXPECT statements: 3-5 short statements about the environment
                - SESSION_FORMATS formats: list the applicable formats
                - CONTACT cta_text: 3-4 words, neutral CTA like "Book a session"
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
                input.contactMethod().name(),
                input.contactValue()
        );
    }

    private String sectionKeys() {
        return Arrays.stream(SectionType.values())
                .map(SectionType::name)
                .collect(Collectors.joining(", "));
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
