package com.ai.therapists.api.page;

import com.ai.therapists.api.section_data.AreasOfSupportData;
import com.ai.therapists.api.section_data.ContactData;
import com.ai.therapists.api.section_data.DisclaimerData;
import com.ai.therapists.api.section_data.FooterData;
import com.ai.therapists.api.section_data.HeaderData;
import com.ai.therapists.api.section_data.HeroData;
import com.ai.therapists.api.section_data.HowIWorkData;
import com.ai.therapists.api.section_data.SessionFormatsData;
import com.ai.therapists.api.section_data.StructuredSections;
import com.ai.therapists.api.section_data.WhatYouCanExpectData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StructuredSectionsMapper {

    private final ObjectMapper objectMapper;

    public StructuredSections fromStorage(Map<SectionType, String> sections) {
        return new StructuredSections(
                parseHeader(sections.get(SectionType.HEADER)),
                parseHero(sections.get(SectionType.HERO)),
                parseAreasOfSupport(sections.get(SectionType.AREAS_OF_SUPPORT)),
                parseHowIWork(sections.get(SectionType.HOW_I_WORK)),
                parseWhatYouCanExpect(sections.get(SectionType.WHAT_YOU_CAN_EXPECT)),
                parseSessionFormats(sections.get(SectionType.SESSION_FORMATS)),
                parseContact(sections.get(SectionType.CONTACT)),
                parseDisclaimer(sections.get(SectionType.DISCLAIMER)),
                parseFooter(sections.get(SectionType.FOOTER))
        );
    }

    private HeaderData parseHeader(String value) {
        HeaderData parsed = readValue(value, HeaderData.class);
        return parsed != null ? parsed : new HeaderData(cleanText(value), "", "", null, null);
    }

    private HeroData parseHero(String value) {
        HeroData parsed = readValue(value, HeroData.class);
        return parsed != null ? parsed : new HeroData(cleanText(value), "");
    }

    private AreasOfSupportData parseAreasOfSupport(String value) {
        AreasOfSupportData parsed = readValue(value, AreasOfSupportData.class);
        return parsed != null ? parsed : new AreasOfSupportData("Areas of support", splitItems(value));
    }

    private HowIWorkData parseHowIWork(String value) {
        HowIWorkData parsed = readValue(value, HowIWorkData.class);
        return parsed != null ? parsed : new HowIWorkData("How I work", cleanText(value));
    }

    private WhatYouCanExpectData parseWhatYouCanExpect(String value) {
        WhatYouCanExpectData parsed = readValue(value, WhatYouCanExpectData.class);
        return parsed != null ? parsed : new WhatYouCanExpectData("What you can expect", splitItems(value));
    }

    private SessionFormatsData parseSessionFormats(String value) {
        SessionFormatsData parsed = readValue(value, SessionFormatsData.class);
        return parsed != null ? parsed : new SessionFormatsData(
                "Session formats",
                List.of(new SessionFormatsData.FormatItem(cleanText(value), ""))
        );
    }

    private ContactData parseContact(String value) {
        ContactData parsed = readValue(value, ContactData.class);
        if (parsed != null) {
            return parsed;
        }

        String cleaned = cleanText(value);
        String email = cleaned.contains("@") ? cleaned : null;
        String phone = cleaned.contains("@") ? null : cleaned;
        return new ContactData("Contact", cleaned, "Contact me", phone, email);
    }

    private DisclaimerData parseDisclaimer(String value) {
        DisclaimerData parsed = readValue(value, DisclaimerData.class);
        return parsed != null ? parsed : new DisclaimerData(cleanText(value));
    }

    private FooterData parseFooter(String value) {
        FooterData parsed = readValue(value, FooterData.class);
        return parsed != null ? parsed : new FooterData(cleanText(value), "", "", null, null);
    }

    private <T> T readValue(String value, Class<T> targetType) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(value, targetType);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> splitItems(String value) {
        String cleaned = cleanText(value);
        if (cleaned.isBlank()) {
            return List.of();
        }

        return List.of(cleaned.split(",|\\n|•|·")).stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private String cleanText(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}