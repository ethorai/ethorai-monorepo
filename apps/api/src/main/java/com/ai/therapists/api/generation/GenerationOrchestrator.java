package com.ai.therapists.api.generation;

import com.ai.therapists.api.event.EntityType;
import com.ai.therapists.api.event.EventLogRepository;
import com.ai.therapists.api.event.EventType;
import com.ai.therapists.api.page.GeneratedPageResponse;
import com.ai.therapists.api.page.LandingPageRepository;
import com.ai.therapists.api.page.PageStatus;
import com.ai.therapists.api.page.SectionType;
import com.ai.therapists.api.profile.TherapistInput;
import com.ai.therapists.api.profile.TherapistProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenerationOrchestrator {

    private final InputNormalizationService normalizationService;
    private final AiGenerationService aiService;
    private final TherapistProfileRepository profileRepo;
    private final LandingPageRepository pageRepo;
    private final EventLogRepository eventLog;

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

        // Step 3 — Generate content via AI
        Map<SectionType, String> sections = aiService.generate(input);

        // Step 4 — Persist landing page
        UUID pageId = pageRepo.insert(profileId, sections, null);
        eventLog.log(EntityType.PAGE, pageId, EventType.GENERATED);

        // Step 5 — Return response
        return new GeneratedPageResponse(
                pageId,
                profileId,
                input.fullName(),
                input.role(),
                sections,
                PageStatus.DRAFT
        );
    }
}
