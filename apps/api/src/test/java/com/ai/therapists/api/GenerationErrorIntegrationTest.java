package com.ai.therapists.api;

import com.ai.therapists.api.generation.AiGenerationException;
import com.ai.therapists.api.generation.AiGenerationService;
import com.ai.therapists.api.event.EventType;
import com.ai.therapists.api.page.SectionType;
import com.ai.therapists.api.profile.ContactMethod;
import com.ai.therapists.api.profile.RoleType;
import com.ai.therapists.api.profile.SessionFormat;
import com.ai.therapists.api.profile.TherapistInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static com.ai.therapists.api.jooq.Tables.EVENT_LOG;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GenerationErrorIntegrationTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("openai.api-key", () -> TestEnvSupport.value("OPENAI_API_KEY"));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private AiGenerationService aiGenerationService;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(aiGenerationService);
    }

    @Test
    void generate_returns422_whenGeneratedContentViolatesGuardrails() throws Exception {
        int eventsBefore = dsl.fetchCount(EVENT_LOG);

        Mockito.when(aiGenerationService.generate(any())).thenReturn(Map.of(
                SectionType.HEADER, "Dr Test - PSYCHOLOGIST",
                SectionType.HERO, "Je travaille avec Adults",
                SectionType.AREAS_OF_SUPPORT, "Stress, Life transitions",
                SectionType.HOW_I_WORK, "I help people heal quickly.",
                SectionType.WHAT_YOU_CAN_EXPECT, "Confidentialité, respect",
                SectionType.SESSION_FORMATS, "ONLINE",
                SectionType.CONTACT, "EMAIL: hello@example.com",
                SectionType.DISCLAIMER, "Les séances ne se substituent pas à un suivi médical.",
                SectionType.FOOTER, "Dr Test - Paris"
        ));

        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("GENERATION_VALIDATION_FAILED")))
                .andExpect(jsonPath("$.message", is("Forbidden term detected: heal")));

            org.junit.jupiter.api.Assertions.assertEquals(eventsBefore + 2, dsl.fetchCount(EVENT_LOG));
            String lastEventType = dsl.select(EVENT_LOG.EVENT_TYPE)
                .from(EVENT_LOG)
                .orderBy(EVENT_LOG.ID.desc())
                .limit(1)
                .fetchOne(EVENT_LOG.EVENT_TYPE);
            org.junit.jupiter.api.Assertions.assertEquals(EventType.GENERATION_FAILED.name(), lastEventType);
    }

    @Test
    void generate_returns502_whenAiServiceFails() throws Exception {
        int eventsBefore = dsl.fetchCount(EVENT_LOG);

        Mockito.when(aiGenerationService.generate(any()))
                .thenThrow(new AiGenerationException("OpenAI upstream error"));

        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload())))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code", is("AI_GENERATION_FAILED")))
                .andExpect(jsonPath("$.message", is("OpenAI upstream error")));

            org.junit.jupiter.api.Assertions.assertEquals(eventsBefore + 2, dsl.fetchCount(EVENT_LOG));
            String lastEventType = dsl.select(EVENT_LOG.EVENT_TYPE)
                .from(EVENT_LOG)
                .orderBy(EVENT_LOG.ID.desc())
                .limit(1)
                .fetchOne(EVENT_LOG.EVENT_TYPE);
            org.junit.jupiter.api.Assertions.assertEquals(EventType.GENERATION_FAILED.name(), lastEventType);
    }

    @Test
    void generate_returns400_whenPayloadIsInvalid() throws Exception {
        TherapistInput invalidPayload = new TherapistInput(
                "",
                RoleType.PSYCHOLOGIST,
                "Paris",
                java.util.List.of(),
                java.util.List.of("Stress"),
                "Integrative",
                SessionFormat.ONLINE,
                java.util.List.of("Confidentiality"),
                ContactMethod.EMAIL,
                ""
        );

        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_INPUT")))
                .andExpect(jsonPath("$.message", is("Request payload is invalid")));
    }

    private TherapistInput validPayload() {
        return new TherapistInput(
                "Dr Test",
                RoleType.PSYCHOLOGIST,
                "Paris",
                java.util.List.of("Adults"),
                java.util.List.of("Stress", "Life transitions"),
                "Integrative",
                SessionFormat.ONLINE,
                java.util.List.of("Confidentiality", "Respect"),
                ContactMethod.EMAIL,
                "hello@example.com"
        );
    }

    @TestConfiguration
    static class StubAiConfig {

        @Bean
        @Primary
        AiGenerationService aiGenerationService() {
            return Mockito.mock(AiGenerationService.class);
        }
    }
}
