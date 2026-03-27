package com.ai.therapists.api.generation;

import com.ai.therapists.api.page.SectionType;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OutputValidationServiceTest {

    private final OutputValidationService service = new OutputValidationService();

    @Test
    void validateOrThrow_acceptsValidSections() {
        assertDoesNotThrow(() -> service.validateOrThrow(validSections()));
    }

    @Test
    void validateOrThrow_rejectsMissingRequiredSection() {
        Map<SectionType, String> sections = validSections();
        sections.remove(SectionType.HERO);

        GenerationValidationException ex = assertThrows(
                GenerationValidationException.class,
                () -> service.validateOrThrow(sections)
        );

        assertEquals("Missing or empty required sections: [HERO]", ex.getMessage());
    }

    @Test
    void validateOrThrow_rejectsMissingDisclaimer() {
        Map<SectionType, String> sections = validSections();
        sections.put(SectionType.DISCLAIMER, "   ");

        GenerationValidationException ex = assertThrows(
                GenerationValidationException.class,
                () -> service.validateOrThrow(sections)
        );

        assertEquals("Missing or empty required sections: [DISCLAIMER]", ex.getMessage());
    }

    @Test
    void validateOrThrow_rejectsForbiddenVocabulary() {
        Map<SectionType, String> sections = validSections();
        sections.put(SectionType.HOW_I_WORK, "I help people heal through guided sessions.");

        GenerationValidationException ex = assertThrows(
                GenerationValidationException.class,
                () -> service.validateOrThrow(sections)
        );

        assertEquals("Forbidden term detected: heal", ex.getMessage());
    }

    private Map<SectionType, String> validSections() {
        Map<SectionType, String> sections = new EnumMap<>(SectionType.class);
        sections.put(SectionType.HEADER, "<h1>Dr Test - Psychologist</h1>");
        sections.put(SectionType.HERO, "<h2>Accompagnement psychologique pour adultes</h2>");
        sections.put(SectionType.AREAS_OF_SUPPORT, "<ul><li>Stress</li></ul>");
        sections.put(SectionType.HOW_I_WORK, "<p>Approche intégrative et collaborative.</p>");
        sections.put(SectionType.WHAT_YOU_CAN_EXPECT, "<p>Un espace confidentiel et respectueux.</p>");
        sections.put(SectionType.SESSION_FORMATS, "<p>Séances en ligne.</p>");
        sections.put(SectionType.CONTACT, "<p>Contact par email.</p>");
        sections.put(SectionType.DISCLAIMER, "<p>Ce service ne remplace pas un avis médical.</p>");
        sections.put(SectionType.FOOTER, "<p>Dr Test - Paris</p>");
        return sections;
    }
}
