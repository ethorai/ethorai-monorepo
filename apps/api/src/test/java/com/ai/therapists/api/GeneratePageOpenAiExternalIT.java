package com.ai.therapists.api;

import com.ai.therapists.api.profile.RoleType;
import com.ai.therapists.api.profile.SessionFormat;
import com.ai.therapists.api.profile.TherapistInput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "00000000-0000-0000-0000-000000000001")
@Tag("external")
@EnabledIfSystemProperty(named = "OPENAI_API_KEY", matches = ".+")
class GeneratePageOpenAiExternalIT {

    static {
        String key = TestEnvSupport.value("OPENAI_API_KEY");
        if (!key.isBlank()) {
            System.setProperty("OPENAI_API_KEY", key);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("openai.api-key", () -> TestEnvSupport.value("OPENAI_API_KEY"));
    }

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.junit.jupiter.api.BeforeEach
    void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void generate_callsRealOpenAiAndReturnsStructuredSections() throws Exception {
        String runId = UUID.randomUUID().toString().substring(0, 8);

        TherapistInput payload = new TherapistInput(
                "Dr External " + runId,
                RoleType.PSYCHOLOGIST,
                "Paris",
                List.of("Adults"),
                List.of("Stress", "Life transitions"),
                "Integrative",
                SessionFormat.ONLINE,
                List.of("Confidentiality", "Respect"),
                null,
                "external+" + runId + "@example.com",
                null,
                null
        );

        // POST → 202 with job
        MvcResult submitResult = mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId", notNullValue()))
                .andReturn();

        String jobId = objectMapper.readTree(submitResult.getResponse().getContentAsString())
                .get("jobId").asText();

        // Poll until completed (real OpenAI call, allow up to 60s)
        String pageId = null;
        for (int i = 0; i < 60; i++) {
            Thread.sleep(1000);
            MvcResult statusResult = mockMvc.perform(get("/api/generate/status/{jobId}", jobId))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode statusNode = objectMapper.readTree(statusResult.getResponse().getContentAsString());
            String jobStatus = statusNode.get("status").asText();
            if ("COMPLETED".equals(jobStatus)) {
                pageId = statusNode.get("pageId").asText();
                break;
            }
            if ("FAILED".equals(jobStatus)) {
                org.junit.jupiter.api.Assertions.fail("Job failed: " + statusNode.get("error").asText());
            }
        }
        org.junit.jupiter.api.Assertions.assertNotNull(pageId, "Job did not complete in time");

        // GET the page and verify structured sections
        mockMvc.perform(get("/api/pages/{id}", pageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is(payload.fullName())))
                .andExpect(jsonPath("$.role", is("PSYCHOLOGIST")))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.sections.HEADER.name", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.HERO.heading", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.AREAS_OF_SUPPORT.items[0].title", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.AREAS_OF_SUPPORT.items[0].description", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.HOW_I_WORK.description", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.WHAT_YOU_CAN_EXPECT.statements[0].title", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.WHAT_YOU_CAN_EXPECT.statements[0].description", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.SESSION_FORMATS.formats[0].type", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.CONTACT.cta_text", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.DISCLAIMER.text", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.FOOTER.name", not(blankOrNullString())));
    }
}
