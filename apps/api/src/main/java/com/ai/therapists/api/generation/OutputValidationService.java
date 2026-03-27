package com.ai.therapists.api.generation;

import com.ai.therapists.api.page.SectionType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OutputValidationService {

    private static final List<String> FORBIDDEN_TERMS = List.of(
            "heal", "healing", "gu\u00e9rir", "gu\u00e9rison",
            "cure", "cured",
            "fix", "fixed",
            "eliminate", "\u00e9liminer",
            "guarantee", "guaranteed", "garantie",
            "results", "r\u00e9sultats",
            "transformation", "transformative",
            "breakthrough", "proven",
            "best", "top", "expert", "life-changing",
            "struggle", "lutte"
    );

    public void validateOrThrow(Map<SectionType, String> sections) {
        validateRequiredSections(sections);
        validateDisclaimer(sections);
        validateForbiddenVocabulary(sections);
    }

    private void validateRequiredSections(Map<SectionType, String> sections) {
        Set<SectionType> missing = Set.of(SectionType.values()).stream()
                .filter(section -> !sections.containsKey(section) || isBlank(sections.get(section)))
                .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            throw new GenerationValidationException("Missing or empty required sections: " + missing);
        }
    }

    private void validateDisclaimer(Map<SectionType, String> sections) {
        String disclaimer = sections.get(SectionType.DISCLAIMER);
        if (isBlank(disclaimer)) {
            throw new GenerationValidationException("Ethical disclaimer is missing");
        }
    }

    private void validateForbiddenVocabulary(Map<SectionType, String> sections) {
        String fullText = sections.values().stream()
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining("\n"))
                .toLowerCase();

        for (String term : FORBIDDEN_TERMS) {
            if (fullText.contains(term.toLowerCase())) {
                throw new GenerationValidationException("Forbidden term detected: " + term);
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
