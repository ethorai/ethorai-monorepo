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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.Map;
import java.util.UUID;

import static com.ai.therapists.api.jooq.Tables.LANDING_PAGE;
import static com.ai.therapists.api.jooq.Tables.THERAPIST_PROFILE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Contract test: POST /api/generate creates a job, poll until done, then GET /api/pages/{id} verifies persistence
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class GetPageIntegrationTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("openai.api-key", () -> TestEnvSupport.value("OPENAI_API_KEY"));
    }

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private DSLContext dsl;

    @org.junit.jupiter.api.BeforeEach
    void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generate_then_get_verifiesPersistedData() throws Exception {
        String runId = UUID.randomUUID().toString().substring(0, 8);
        String fullName = "Dr Verify " + runId;
        String role = "PSYCHOLOGIST";
        String email = "verify+" + runId + "@example.com";

        TherapistInput payload = new TherapistInput(
                fullName,
                RoleType.PSYCHOLOGIST,
                "Lyon",
                java.util.List.of("Teenagers"),
                java.util.List.of("Anxiety"),
                "CBT",
                SessionFormat.BOTH,
                java.util.List.of("Privacy"),
                ContactMethod.EMAIL,
                email
        );

        // Step 1: POST to generate → 202 with job ID
        MvcResult submitResult = mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId", notNullValue()))
                .andReturn();

        String jobId = objectMapper.readTree(submitResult.getResponse().getContentAsString())
                .get("jobId").asText();

        // Step 2: Poll until COMPLETED
        String pageId = pollUntilCompleted(jobId);

        // Step 3: GET /api/pages/{id} to verify persistence
        mockMvc.perform(get("/api/pages/{id}", pageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageId", is(pageId)))
                .andExpect(jsonPath("$.profileId", notNullValue()))
                .andExpect(jsonPath("$.fullName", is(fullName)))
                .andExpect(jsonPath("$.role", is(role)))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.sections").exists())
                .andExpect(jsonPath("$.sections", notNullValue()));
    }

    @Test
    void get_pageNotFound_returns404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/pages/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getJobStatus_notFound_returns404() throws Exception {
        UUID nonExistentJobId = UUID.randomUUID();
        mockMvc.perform(get("/api/generate/status/{jobId}", nonExistentJobId))
                .andExpect(status().isNotFound());
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
