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
                FORBIDDEN words: heal, healing, cure, cured, fix, fixed, eliminate, guarantee, guaranteed, \
                results, transformation, transformative, breakthrough, proven, best, top, expert, life-changing, struggle.
                USE INSTEAD: support, work with, accompany, explore, provide a space, collaborative, adapted to each person.

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
                Generate a landing page for the therapist below. Return a JSON object with exactly these keys: %s.
                Each value is the HTML content for that section (use simple HTML: <h1>, <h2>, <p>, <ul>, <li>, no classes or styles).

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

                Return ONLY the JSON object, no markdown fences, no explanation.
                """.formatted(
                sectionKeys(),
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
