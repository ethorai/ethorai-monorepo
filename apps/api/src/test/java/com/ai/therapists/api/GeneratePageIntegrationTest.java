package com.ai.therapists.api;

import com.ai.therapists.api.generation.AiGenerationService;
import com.ai.therapists.api.page.SectionType;
import com.ai.therapists.api.profile.ContactMethod;
import com.ai.therapists.api.profile.RoleType;
import com.ai.therapists.api.profile.SessionFormat;
import com.ai.therapists.api.profile.TherapistInput;
import com.ai.therapists.api.test.StructuredSectionsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
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
import java.util.UUID;

import static com.ai.therapists.api.jooq.Tables.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// smoke test
@SpringBootTest
@AutoConfigureMockMvc
class GeneratePageIntegrationTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("openai.api-key", () -> TestEnvSupport.value("OPENAI_API_KEY"));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generate_createsJobAndCompletesAsynchronously() throws Exception {
        int profilesBefore = dsl.fetchCount(THERAPIST_PROFILE);
        int pagesBefore = dsl.fetchCount(LANDING_PAGE);
        int eventsBefore = dsl.fetchCount(EVENT_LOG);
        int jobsBefore = dsl.fetchCount(GENERATION_JOB);

        String runId = UUID.randomUUID().toString().substring(0, 8);
        String fullName = "Dr Test " + runId;
        String email = "hello+" + runId + "@example.com";

        TherapistInput payload = new TherapistInput(
            fullName,
            RoleType.PSYCHOLOGIST,
            "Paris",
            java.util.List.of("Adults"),
            java.util.List.of("Stress", "Life transitions"),
            "Integrative",
            SessionFormat.ONLINE,
            java.util.List.of("Confidentiality", "Respect"),
            ContactMethod.EMAIL,
            email
        );

        // Step 1: POST /api/generate → 202 Accepted with job ID
        MvcResult submitResult = mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId", notNullValue()))
                .andExpect(jsonPath("$.profileId", notNullValue()))
                .andExpect(jsonPath("$.status").exists())
                .andReturn();

        String jobId = objectMapper.readTree(submitResult.getResponse().getContentAsString())
                .get("jobId").asText();

        // Step 2: Poll status until COMPLETED
        String pageId = pollUntilCompleted(jobId);

        org.junit.jupiter.api.Assertions.assertNotNull(pageId);
        org.junit.jupiter.api.Assertions.assertEquals(profilesBefore + 1, dsl.fetchCount(THERAPIST_PROFILE));
        org.junit.jupiter.api.Assertions.assertEquals(pagesBefore + 1, dsl.fetchCount(LANDING_PAGE));
        org.junit.jupiter.api.Assertions.assertEquals(jobsBefore + 1, dsl.fetchCount(GENERATION_JOB));
        // Events: PROFILE CREATED + PAGE GENERATED
        org.junit.jupiter.api.Assertions.assertEquals(eventsBefore + 2, dsl.fetchCount(EVENT_LOG));
    }

    private String pollUntilCompleted(String jobId) throws Exception {
        for (int i = 0; i < 20; i++) {
            Thread.sleep(200);
            MvcResult statusResult = mockMvc.perform(get("/api/generate/status/{jobId}", jobId))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode statusNode = objectMapper.readTree(statusResult.getResponse().getContentAsString());
            String status = statusNode.get("status").asText();

            if ("COMPLETED".equals(status)) {
                return statusNode.get("pageId").asText();
            }
            if ("FAILED".equals(status)) {
                org.junit.jupiter.api.Assertions.fail("Job failed: " + statusNode.get("error").asText());
            }
        }
        org.junit.jupiter.api.Assertions.fail("Job did not complete in time");
        return null;
    }

    @TestConfiguration
    static class StubAiConfig {

        @Bean
        @Primary
        AiGenerationService aiGenerationService() {
            AiGenerationService mock = Mockito.mock(AiGenerationService.class);
            StructuredSectionsBuilder builder = new StructuredSectionsBuilder();
            Mockito.when(mock.generate(any())).thenReturn(builder.buildTestSections());
            return mock;
        }
    }
}
