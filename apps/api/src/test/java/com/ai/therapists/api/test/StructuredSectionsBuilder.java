package com.ai.therapists.api.test;

import com.ai.therapists.api.page.SectionType;
import com.ai.therapists.api.section_data.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class StructuredSectionsBuilder {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<SectionType, String> buildTestSections() {
        StructuredSections sections = new StructuredSections(
            new HeaderData("Dr Test", "PSYCHOLOGIST", "Paris", null, "hello@example.com"),
            new HeroData("Je travaille avec adultes", "Accompagnement en psychologie"),
            new AreasOfSupportData(
                "Domaines d'accompagnement",
                List.of("Stress", "Transitions de vie", "Anxiété")
            ),
            new HowIWorkData(
                "Ma pratique",
                "Je propose une approche intégrative et collaborative."
            ),
            new WhatYouCanExpectData(
                "Ce que vous pouvez attendre",
                List.of(
                    "Un espace confidentiel et sécurisé",
                    "De l'écoute et du respect",
                    "Une collaboration adaptée à votre rythme"
                )
            ),
            new SessionFormatsData(
                "Formats de séances",
                List.of(
                    new SessionFormatsData.FormatItem("ONLINE", "Séances en ligne via vidéoconférence"),
                    new SessionFormatsData.FormatItem("IN_PERSON", "Séances en cabinet à Paris")
                )
            ),
            new ContactData(
                "Contact et prise de rendez-vous",
                "Contactez-moi pour une première séance",
                "Prendre rendez-vous",
                null,
                "hello@example.com"
            ),
            new DisclaimerData("Les séances de psychologie ne se substituent pas à un suivi médical. En cas de crise, veuillez contacter les services d'urgence."),
            new FooterData("Dr Test", "Psychologue", "Paris", null, "hello@example.com")
        );
        
        Map<SectionType, String> map = new EnumMap<>(SectionType.class);
        try {
            map.put(SectionType.HEADER, objectMapper.writeValueAsString(sections.header()));
            map.put(SectionType.HERO, objectMapper.writeValueAsString(sections.hero()));
            map.put(SectionType.AREAS_OF_SUPPORT, objectMapper.writeValueAsString(sections.areasOfSupport()));
            map.put(SectionType.HOW_I_WORK, objectMapper.writeValueAsString(sections.howIWork()));
            map.put(SectionType.WHAT_YOU_CAN_EXPECT, objectMapper.writeValueAsString(sections.whatYouCanExpect()));
            map.put(SectionType.SESSION_FORMATS, objectMapper.writeValueAsString(sections.sessionFormats()));
            map.put(SectionType.CONTACT, objectMapper.writeValueAsString(sections.contact()));
            map.put(SectionType.DISCLAIMER, objectMapper.writeValueAsString(sections.disclaimer()));
            map.put(SectionType.FOOTER, objectMapper.writeValueAsString(sections.footer()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}
