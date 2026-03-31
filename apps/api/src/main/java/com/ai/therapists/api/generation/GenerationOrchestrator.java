package com.ai.therapists.api.generation;

import com.ai.therapists.api.event.EntityType;
import com.ai.therapists.api.event.EventLogRepository;
import com.ai.therapists.api.event.EventType;
import com.ai.therapists.api.page.GeneratedPageResponse;
import com.ai.therapists.api.page.LandingPageRepository;
import com.ai.therapists.api.page.PageStatus;
import com.ai.therapists.api.page.SectionType;
import com.ai.therapists.api.page.StructuredSectionsMapper;
import com.ai.therapists.api.profile.TherapistInput;
import com.ai.therapists.api.profile.TherapistProfileRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.JSONB;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenerationOrchestrator {

    private final InputNormalizationService normalizationService;
    private final AiGenerationService aiService;
    private final OutputValidationService outputValidationService;
    private final TherapistProfileRepository profileRepo;
    private final LandingPageRepository pageRepo;
    private final EventLogRepository eventLog;
    private final StructuredSectionsMapper structuredSectionsMapper;

    public GeneratedPageResponse execute(TherapistInput rawInput) {
        // Step 1 — Normalize inputs
        TherapistInput input = normalizationService.normalize(rawInput);

        // Step 2 — Persist therapist profile
        UUID profileId = profileRepo.insert(
                input.fullName(),
                input.role(),
                input.location(),
                input.audiences(),
                input.areasOfSupport(),
                input.approach(),
                input.sessionFormat(),
                input.expectations(),
                input.contactMethod(),
                input.contactValue()
        );
        eventLog.log(EntityType.PROFILE, profileId, EventType.CREATED);

        try {
            // Step 3 — Generate content via AI
            Map<SectionType, String> sections = aiService.generate(input);

            // Step 4 — Validate generated content against guardrails
            outputValidationService.validateOrThrow(sections);

            // Step 5 — Persist landing page
            UUID pageId = pageRepo.insert(profileId, sections, null);
            eventLog.log(EntityType.PAGE, pageId, EventType.GENERATED);

            // Step 6 — Return response
            return new GeneratedPageResponse(
                    pageId,
                    profileId,
                    input.fullName(),
                    input.role(),
                        structuredSectionsMapper.fromStorage(sections),
                    PageStatus.DRAFT
            );
        } catch (AiGenerationException | GenerationValidationException ex) {
            eventLog.log(
                    EntityType.PROFILE,
                    profileId,
                    EventType.GENERATION_FAILED,
                    JSONB.jsonb(toFailurePayload(ex))
            );
            throw ex;
        }
    }

    private String toFailurePayload(Exception ex) {
        String escapedMessage = ex.getMessage() == null ? "unknown" : ex.getMessage().replace("\"", "\\\"");
        return "{\"error\":\"" + escapedMessage + "\",\"type\":\"" + ex.getClass().getSimpleName() + "\"}";
    }
}
