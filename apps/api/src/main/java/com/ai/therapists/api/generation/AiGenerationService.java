package com.ai.therapists.api.generation;

import com.ai.therapists.api.page.SectionType;
import com.ai.therapists.api.profile.TherapistInput;
import com.ai.therapists.api.section_data.StructuredSections;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AiGenerationService.class);

    private final PromptAssemblyService promptService;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4o}")
    private String model;

    @Value("${openai.temperature:0.4}")
    private double temperature;

    public Map<SectionType, String> generate(TherapistInput input) {
        String systemPrompt = promptService.assembleSystemPrompt(input);
        String userPrompt = promptService.assembleUserPrompt(input);

        String json = callOpenAi(systemPrompt, userPrompt);
        return parseResponse(json);
    }

    public String regenerateSection(TherapistInput input, SectionType sectionType, String currentContent) {
        String systemPrompt = promptService.assembleSystemPrompt(input);
        String userPrompt = promptService.assembleSectionRegenerationPrompt(input, sectionType, currentContent);

        String json = callOpenAi(systemPrompt, userPrompt);
        return cleanJsonFences(json);
    }

    private String callOpenAi(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiGenerationException("OPENAI_API_KEY is missing. Set it in your environment or IntelliJ Run Configuration.");
        }

        RestClient client = restClientBuilder
                .baseUrl("https://api.openai.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", temperature,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        var response = client.post()
                .uri("/v1/chat/completions")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(OpenAiResponse.class);

        if (response == null || response.choices().isEmpty()) {
            throw new AiGenerationException("Empty response from OpenAI");
        }

        return response.choices().getFirst().message().content();
    }

    private Map<SectionType, String> parseResponse(String json) {
        String cleaned = cleanJsonFences(json);

        try {
            // Parse the structured JSON response
            StructuredSections structured = objectMapper.readValue(cleaned, StructuredSections.class);
            
            Map<SectionType, String> sections = new EnumMap<>(SectionType.class);
            
            // Serialize each section data back to JSON string for storage
            sections.put(SectionType.HEADER, objectMapper.writeValueAsString(structured.header()));
            sections.put(SectionType.HERO, objectMapper.writeValueAsString(structured.hero()));
            sections.put(SectionType.AREAS_OF_SUPPORT, objectMapper.writeValueAsString(structured.areasOfSupport()));
            sections.put(SectionType.HOW_I_WORK, objectMapper.writeValueAsString(structured.howIWork()));
            sections.put(SectionType.WHAT_YOU_CAN_EXPECT, objectMapper.writeValueAsString(structured.whatYouCanExpect()));
            sections.put(SectionType.SESSION_FORMATS, objectMapper.writeValueAsString(structured.sessionFormats()));
            sections.put(SectionType.CONTACT, objectMapper.writeValueAsString(structured.contact()));
            sections.put(SectionType.DISCLAIMER, objectMapper.writeValueAsString(structured.disclaimer()));
            sections.put(SectionType.FOOTER, objectMapper.writeValueAsString(structured.footer()));
            
            return sections;
        } catch (Exception e) {
            throw new AiGenerationException("Failed to parse AI response: " + e.getMessage());
        }
    }

    // --- helpers ---

    private String cleanJsonFences(String json) {
        String cleaned = json.strip();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```[a-z]*\\n?", "").replaceFirst("```$", "").strip();
        }
        return cleaned;
    }

    // --- OpenAI response records ---

    record OpenAiResponse(List<Choice> choices) {}
    record Choice(Message message) {}
    record Message(String content) {}
}
