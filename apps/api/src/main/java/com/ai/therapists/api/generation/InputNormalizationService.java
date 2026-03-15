package com.ai.therapists.api.generation;

import com.ai.therapists.api.profile.TherapistInput;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InputNormalizationService {

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
                trimOrNull(input.fullName()),
                input.role(),
                trimOrNull(input.location()),
                normalizeList(input.audiences()),
                normalizeList(input.areasOfSupport()),
                sanitizeText(trimOrNull(input.approach())),
                input.sessionFormat(),
                normalizeList(input.expectations()),
                input.contactMethod(),
                trimOrNull(input.contactValue())
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
        for (String[] replacement : REPLACEMENTS) {
            result = result.replaceAll("(?i)\\b" + replacement[0] + "\\b", replacement[1]);
        }
        // Remove emojis (Unicode emoji ranges)
        result = result.replaceAll("[\\p{So}\\p{Cn}]", "");
        return result.trim();
    }

    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
