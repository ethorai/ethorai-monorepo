package com.ai.therapists.api;

import com.ai.therapists.api.profile.ContactMethod;
import com.ai.therapists.api.profile.RoleType;
import com.ai.therapists.api.profile.SessionFormat;
import com.ai.therapists.api.profile.TherapistInput;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                ContactMethod.EMAIL,
                "external+" + runId + "@example.com"
        );

        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName", is(payload.fullName())))
                .andExpect(jsonPath("$.role", is("PSYCHOLOGIST")))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.sections.HEADER", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.HERO", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.AREAS_OF_SUPPORT", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.HOW_I_WORK", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.WHAT_YOU_CAN_EXPECT", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.SESSION_FORMATS", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.CONTACT", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.DISCLAIMER", not(blankOrNullString())))
                .andExpect(jsonPath("$.sections.FOOTER", not(blankOrNullString())));
    }
}
