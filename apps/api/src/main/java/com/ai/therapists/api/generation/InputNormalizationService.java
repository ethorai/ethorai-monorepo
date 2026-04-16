package com.ai.therapists.api.generation;

import com.ai.therapists.api.profile.TherapistInput;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class InputNormalizationService {

    private static final int MAX_SHORT_TEXT_LENGTH = 150;
    private static final int MAX_APPROACH_LENGTH = 500;

    // Patterns that indicate prompt injection attempts
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)\\bignore\\s+(previous|above|all|prior)\\s+instructions?\\b"),
            Pattern.compile("(?i)\\bdisregard\\s+(all|previous|above|prior)\\b"),
            Pattern.compile("(?i)\\boverride\\s+(the\\s+)?(system|instructions?|rules?|guardrails?)\\b"),
            Pattern.compile("(?i)^\\s*(system|user|assistant|instruction|prompt)\\s*:", Pattern.MULTILINE),
            Pattern.compile("(?m)^\\s*[-=*]{3,}\\s*$")  // separator lines like ---, ===, ***
    );

    private static final List<String> FORBIDDEN_WORDS = List.of(
            "guérir", "guérison", "heal", "healing",
            "cure", "cured",
            "fix", "fixed",
            "éliminer", "eliminate",
            "garantie", "guarantee", "guaranteed",
            "résultats", "results",
            "transformation", "transformative",
            "breakthrough", "percée",
            "prouvé", "proven",
            "meilleur", "best", "top", "expert",
            "life-changing", "lutte", "struggle"
    );

    private static final String[][] REPLACEMENTS = {
            {"lutte", "difficulté"},
            {"struggle", "difficulty"},
            {"guérir", "accompagner"},
            {"heal", "support"},
            {"fix", "work through"},
            {"cure", "support"}
    };

    public TherapistInput normalize(TherapistInput input) {
        return new TherapistInput(
                truncate(trimOrNull(input.fullName()), MAX_SHORT_TEXT_LENGTH),
                input.role(),
                truncate(trimOrNull(input.location()), MAX_SHORT_TEXT_LENGTH),
                normalizeList(input.audiences()),
                normalizeList(input.areasOfSupport()),
                truncate(sanitizeText(trimOrNull(input.approach())), MAX_APPROACH_LENGTH),
                input.sessionFormat(),
                normalizeList(input.expectations()),
                input.contactMethod(),
                truncate(trimOrNull(input.contactValue()), MAX_SHORT_TEXT_LENGTH)
        );
    }

    private List<String> normalizeList(List<String> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::sanitizeText)
                .distinct()
                .toList();
    }

    private String sanitizeText(String text) {
        if (text == null) return null;
        String result = text;

        // Strip prompt injection patterns
        for (Pattern pattern : INJECTION_PATTERNS) {
            result = pattern.matcher(result).replaceAll(" ");
        }

        // Apply forbidden-word soft replacements
        for (String[] replacement : REPLACEMENTS) {
            result = result.replaceAll("(?i)\\b" + replacement[0] + "\\b", replacement[1]);
        }

        // Remove emojis
        result = result.replaceAll("[\\p{So}\\p{Cn}]", "");

        // Collapse multiple whitespace and newlines to a single space
        result = result.replaceAll("\\s+", " ");

        return result.trim();
    }

    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
