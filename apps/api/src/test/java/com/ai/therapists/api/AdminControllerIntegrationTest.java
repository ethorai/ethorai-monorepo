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
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerIntegrationTest {

    private static final UUID ADMIN_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID REGULAR_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-0000000000a2");

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

        for (UUID id : List.of(ADMIN_USER_ID, REGULAR_USER_ID)) {
            dsl.deleteFrom(GENERATION_JOB)
                    .where(GENERATION_JOB.PAGE_ID.in(
                            dsl.select(LANDING_PAGE.ID)
                                    .from(LANDING_PAGE)
                                    .where(LANDING_PAGE.USER_ID.eq(id))))
                    .execute();
            dsl.deleteFrom(LANDING_PAGE).where(LANDING_PAGE.USER_ID.eq(id)).execute();
            dsl.insertInto(APP_USER)
                    .set(APP_USER.ID, id)
                    .set(APP_USER.EMAIL, "admin-test-" + id + "@example.com")
                    .set(APP_USER.IS_ADMIN, id.equals(ADMIN_USER_ID))
                    .onConflict(APP_USER.ID).doUpdate()
                    .set(APP_USER.IS_ADMIN, id.equals(ADMIN_USER_ID))
                    .execute();
        }
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-0000000000a2")
    void listUsers_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-0000000000a1", authorities = "ROLE_ADMIN")
    void listUsers_asAdmin_returns200WithUserList() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-0000000000a1", authorities = "ROLE_ADMIN")
    void listUsers_includesRegisteredUser() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.userId == '" + REGULAR_USER_ID + "')]", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-0000000000a1", authorities = "ROLE_ADMIN")
    void getUserDetail_asAdmin_returnsUserInfo() throws Exception {
        mockMvc.perform(get("/api/admin/users/{userId}", REGULAR_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(REGULAR_USER_ID.toString())))
                .andExpect(jsonPath("$.email", is("admin-test-" + REGULAR_USER_ID + "@example.com")));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-0000000000a1", authorities = "ROLE_ADMIN")
    void getUserDetail_withPage_includesPageData() throws Exception {
        TherapistInput payload = new TherapistInput(
                "Dr Admin Test",
                RoleType.PSYCHOLOGIST,
                "Paris",
                List.of("Adultes"),
                List.of("Anxiety"),
                "CBT",
                SessionFormat.ONLINE,
                List.of("Privacy"),
                null,
                "admin-test-page@example.com",
                null,
                null,
                null, null, null, null
        );

        // Generate a page for REGULAR_USER_ID
        MvcResult submit = mockMvc.perform(post("/api/generate")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(REGULAR_USER_ID.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isAccepted())
                .andReturn();

        String jobId = objectMapper.readTree(submit.getResponse().getContentAsString())
                .get("jobId").asText();

        // Poll until complete
        for (int i = 0; i < 20; i++) {
            Thread.sleep(200);
            MvcResult statusResult = mockMvc.perform(get("/api/generate/status/{jobId}", jobId)
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                    .user(REGULAR_USER_ID.toString())))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode node = objectMapper.readTree(statusResult.getResponse().getContentAsString());
            String state = node.get("status").asText();
            if ("COMPLETED".equals(state)) break;
            if ("FAILED".equals(state)) {
                org.junit.jupiter.api.Assertions.fail("Job failed: " + node.get("error").asText());
            }
        }

        // Admin can see the user's page
        mockMvc.perform(get("/api/admin/users/{userId}", REGULAR_USER_ID)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(ADMIN_USER_ID.toString())
                                .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.fullName", is("Dr Admin Test")));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-0000000000a1", authorities = "ROLE_ADMIN")
    void getUserDetail_unknownUser_returns404() throws Exception {
        mockMvc.perform(get("/api/admin/users/{userId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
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
