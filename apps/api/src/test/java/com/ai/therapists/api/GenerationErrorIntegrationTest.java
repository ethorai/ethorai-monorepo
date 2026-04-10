package com.ai.therapists.api;

import com.ai.therapists.api.generation.AiGenerationException;
import com.ai.therapists.api.generation.AiGenerationService;
import com.ai.therapists.api.event.EventType;
import com.ai.therapists.api.page.SectionType;
import com.ai.therapists.api.profile.ContactMethod;
import com.ai.therapists.api.profile.RoleType;
import com.ai.therapists.api.profile.SessionFormat;
import com.ai.therapists.api.profile.TherapistInput;
import com.ai.therapists.api.section_data.HowIWorkData;
import com.ai.therapists.api.test.StructuredSectionsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static com.ai.therapists.api.jooq.Tables.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "00000000-0000-0000-0000-000000000001")
class GenerationErrorIntegrationTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("openai.api-key", () -> TestEnvSupport.value("OPENAI_API_KEY"));
    }

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private AiGenerationService aiGenerationService;

    @BeforeEach
    void setup() {
        Mockito.reset(aiGenerationService);
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void generate_jobFails_whenGeneratedContentViolatesGuardrails() throws Exception {
        int eventsBefore = dsl.fetchCount(EVENT_LOG);

        Map<SectionType, String> invalidSections = new StructuredSectionsBuilder().buildTestSections();
        // Inject forbidden term into HOW_I_WORK
        ObjectMapper mapper = new ObjectMapper();
        HowIWorkData invalidData = new HowIWorkData("Ma pratique", "I help people heal quickly.");
        invalidSections.put(SectionType.HOW_I_WORK, mapper.writeValueAsString(invalidData));
        
        Mockito.when(aiGenerationService.generate(any())).thenReturn(invalidSections);

        // Submit job → 202
        MvcResult submitResult = mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId", notNullValue()))
                .andReturn();

        String jobId = objectMapper.readTree(submitResult.getResponse().getContentAsString())
                .get("jobId").asText();

        // Poll until FAILED
        String error = pollUntilFailed(jobId);

        org.junit.jupiter.api.Assertions.assertTrue(error.contains("Forbidden term detected: heal"));
        // Events: PROFILE CREATED + GENERATION_FAILED
        org.junit.jupiter.api.Assertions.assertEquals(eventsBefore + 2, dsl.fetchCount(EVENT_LOG));
    }

    @Test
    void generate_jobFails_whenAiServiceFails() throws Exception {
        int eventsBefore = dsl.fetchCount(EVENT_LOG);

        Mockito.when(aiGenerationService.generate(any()))
                .thenThrow(new AiGenerationException("OpenAI upstream error"));

        MvcResult submitResult = mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId", notNullValue()))
                .andReturn();

        String jobId = objectMapper.readTree(submitResult.getResponse().getContentAsString())
                .get("jobId").asText();

        String error = pollUntilFailed(jobId);

        org.junit.jupiter.api.Assertions.assertEquals("OpenAI upstream error", error);
        org.junit.jupiter.api.Assertions.assertEquals(eventsBefore + 2, dsl.fetchCount(EVENT_LOG));
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

    private String pollUntilFailed(String jobId) throws Exception {
        for (int i = 0; i < 20; i++) {
            Thread.sleep(200);
            MvcResult statusResult = mockMvc.perform(get("/api/generate/status/{jobId}", jobId))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode statusNode = objectMapper.readTree(statusResult.getResponse().getContentAsString());
            String status = statusNode.get("status").asText();

            if ("FAILED".equals(status)) {
                return statusNode.get("error").asText();
            }
            if ("COMPLETED".equals(status)) {
                org.junit.jupiter.api.Assertions.fail("Expected job to fail but it completed");
            }
        }
        org.junit.jupiter.api.Assertions.fail("Job did not fail in time");
        return null;
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
