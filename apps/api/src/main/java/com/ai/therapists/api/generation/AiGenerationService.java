package com.ai.therapists.api.generation;

import com.ai.therapists.api.page.SectionType;
import com.ai.therapists.api.profile.TherapistInput;
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
        // Strip markdown fences if AI wraps output in ```json ... ```
        String cleaned = json.strip();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```[a-z]*\\n?", "").replaceFirst("```$", "").strip();
        }

        try {
            Map<String, String> raw = objectMapper.readValue(cleaned, new TypeReference<>() {});
            Map<SectionType, String> sections = new EnumMap<>(SectionType.class);
            for (var entry : raw.entrySet()) {
                try {
                    sections.put(SectionType.valueOf(entry.getKey()), entry.getValue());
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown section key from AI: {}", entry.getKey());
                }
            }
            return sections;
        } catch (Exception e) {
            throw new AiGenerationException("Failed to parse AI response: " + e.getMessage());
        }
    }

    // --- OpenAI response records ---

    record OpenAiResponse(List<Choice> choices) {}
    record Choice(Message message) {}
    record Message(String content) {}
}
