package com.ai.therapists.api;

import com.ai.therapists.api.generation.AiGenerationService;
import com.ai.therapists.api.profile.RoleType;
import com.ai.therapists.api.profile.SessionFormat;
import com.ai.therapists.api.profile.TherapistInput;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static com.ai.therapists.api.jooq.Tables.APP_USER;
import static com.ai.therapists.api.jooq.Tables.GENERATION_JOB;
import static com.ai.therapists.api.jooq.Tables.LANDING_PAGE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test isolation: a dedicated user UUID per test, with a pre-test cleanup
// of any landing pages owned by that UUID — keeps the suite reorderable.
@SpringBootTest
@AutoConfigureMockMvc
class MeControllerIntegrationTest {

    private static final UUID EMPTY_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000091");
    private static final UUID GENERATED_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000092");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("openai.api-key", () -> TestEnvSupport.value("OPENAI_API_KEY"));
    }

    @Autowired private WebApplicationContext context;
    @Autowired private DSLContext dsl;
    @Autowired private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        for (UUID id : List.of(EMPTY_USER_ID, GENERATED_USER_ID)) {
            // generation_job.page_id has FK to landing_page — clear jobs first.
            dsl.deleteFrom(GENERATION_JOB)
                    .where(GENERATION_JOB.PAGE_ID.in(
                            dsl.select(LANDING_PAGE.ID)
                                    .from(LANDING_PAGE)
                                    .where(LANDING_PAGE.USER_ID.eq(id))
                    ))
                    .execute();
            dsl.deleteFrom(LANDING_PAGE)
                    .where(LANDING_PAGE.USER_ID.eq(id))
                    .execute();
            dsl.insertInto(APP_USER)
                    .set(APP_USER.ID, id)
                    .set(APP_USER.EMAIL, "me-" + id + "@example.com")
                    .onConflictDoNothing()
                    .execute();
        }
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000091")
    void getMyPage_whenUserHasNoPage_returns204() throws Exception {
        mockMvc.perform(get("/api/me/page"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000091")
    void getMyProfile_whenUserHasNoPage_returns204() throws Exception {
        mockMvc.perform(get("/api/me/profile"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000092")
    void getMyPage_afterGeneration_returnsLatestPage() throws Exception {
        TherapistInput payload = sampleInput("Dr Me Page");
        String pageId = generateAndPoll(payload);

        mockMvc.perform(get("/api/me/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageId", is(pageId)))
                .andExpect(jsonPath("$.fullName", is("Dr Me Page")))
                .andExpect(jsonPath("$.role", is("PSYCHOLOGIST")))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.sections", notNullValue()));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000092")
    void getMyProfile_afterGeneration_returnsInputData() throws Exception {
        TherapistInput payload = sampleInput("Dr Me Profile");
        generateAndPoll(payload);

        mockMvc.perform(get("/api/me/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Dr Me Profile")))
                .andExpect(jsonPath("$.role", is("PSYCHOLOGIST")))
                .andExpect(jsonPath("$.location", is("Lyon")))
                .andExpect(jsonPath("$.audiences[0]", is("Adultes")))
                .andExpect(jsonPath("$.email", notNullValue()));
    }

    private TherapistInput sampleInput(String fullName) {
        return new TherapistInput(
                fullName,
                RoleType.PSYCHOLOGIST,
                "Lyon",
                List.of("Adultes"),
                List.of("Anxiety"),
                "CBT",
                SessionFormat.BOTH,
                List.of("Privacy"),
                null,
                "me+" + UUID.randomUUID().toString().substring(0, 8) + "@example.com",
                null
        );
    }

    private String generateAndPoll(TherapistInput payload) throws Exception {
        MvcResult submit = mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isAccepted())
                .andReturn();

        String jobId = objectMapper.readTree(submit.getResponse().getContentAsString())
                .get("jobId").asText();

        for (int i = 0; i < 20; i++) {
            Thread.sleep(200);
            MvcResult statusResult = mockMvc.perform(get("/api/generate/status/{jobId}", jobId))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode node = objectMapper.readTree(statusResult.getResponse().getContentAsString());
            String state = node.get("status").asText();
            if ("COMPLETED".equals(state)) return node.get("pageId").asText();
            if ("FAILED".equals(state)) {
                org.junit.jupiter.api.Assertions.fail("Job failed: " + node.get("error").asText());
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
