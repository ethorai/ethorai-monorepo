package com.ai.therapists.api;

import com.ai.therapists.api.generation.AiGenerationService;
import com.ai.therapists.api.generation.GenerationRateLimiter;
import com.ai.therapists.api.generation.RateLimitExceededException;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static com.ai.therapists.api.jooq.Tables.APP_USER;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "00000000-0000-0000-0000-000000000001")
class RateLimitIntegrationTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("openai.api-key", () -> TestEnvSupport.value("OPENAI_API_KEY"));
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private GenerationRateLimiter rateLimiter;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        Mockito.reset(rateLimiter);
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        dsl.insertInto(APP_USER)
                .set(APP_USER.ID, UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .set(APP_USER.EMAIL, "test@example.com")
                .onConflictDoNothing()
                .execute();
    }

    @Test
    void generate_returns429_whenRateLimitExceeded() throws Exception {
        Mockito.doThrow(new RateLimitExceededException()).when(rateLimiter).consume(any(UUID.class));

        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload())))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code", is("RATE_LIMIT_EXCEEDED")));
    }

    @Test
    void generate_returns202_whenRateLimitNotExceeded() throws Exception {
        Mockito.doNothing().when(rateLimiter).consume(any(UUID.class));

        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload())))
                .andExpect(status().isAccepted());
    }

    private TherapistInput validPayload() {
        return new TherapistInput(
                "Dr Rate Test",
                RoleType.PSYCHOLOGIST,
                "Paris",
                List.of("Adults"),
                List.of("Stress"),
                "Integrative",
                SessionFormat.ONLINE,
                List.of("Confidentiality"),
                null,
                "rate@example.com",
                null
        );
    }

    @TestConfiguration
    static class StubConfig {

        @Bean
        @Primary
        GenerationRateLimiter rateLimiter() {
            return Mockito.mock(GenerationRateLimiter.class);
        }

        @Bean
        @Primary
        AiGenerationService aiGenerationService() {
            return Mockito.mock(AiGenerationService.class);
        }
    }
}
