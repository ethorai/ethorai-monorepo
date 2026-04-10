package com.ai.therapists.api.generation;

import com.ai.therapists.api.event.EntityType;
import com.ai.therapists.api.event.EventLogRepository;
import com.ai.therapists.api.event.EventType;
import com.ai.therapists.api.generation.GenerationJobRepository.GenerationJobRow;
import com.ai.therapists.api.page.GeneratedPageResponse;
import com.ai.therapists.api.page.LandingPageRepository;
import com.ai.therapists.api.page.LandingPageRepository.LandingPageRow;
import com.ai.therapists.api.page.SectionType;
import com.ai.therapists.api.page.StructuredSectionsMapper;
import com.ai.therapists.api.profile.TherapistInput;
import com.ai.therapists.api.profile.TherapistProfileRepository;
import com.ai.therapists.api.profile.TherapistProfileRepository.TherapistProfileRow;
import lombok.RequiredArgsConstructor;
import org.jooq.JSONB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenerationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(GenerationOrchestrator.class);

    private final InputNormalizationService normalizationService;
    private final AiGenerationService aiService;
    private final OutputValidationService outputValidationService;
    private final TherapistProfileRepository profileRepo;
    private final LandingPageRepository pageRepo;
    private final EventLogRepository eventLog;
    private final StructuredSectionsMapper structuredSectionsMapper;
    private final GenerationJobRepository jobRepo;

    /**
     * Submit a generation job asynchronously. Returns the job ID immediately.
     * The actual generation runs in a background thread via processJob().
     */
    public GenerationJobResponse submitAsync(TherapistInput rawInput, UUID userId) {
        TherapistInput input = normalizationService.normalize(rawInput);

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

        UUID jobId = jobRepo.insert(profileId);

        processJobAsync(jobId, profileId, userId, input);

        GenerationJobRow job = jobRepo.findById(jobId).orElseThrow();
        return toJobResponse(job);
    }

    @Async
    public void processJobAsync(UUID jobId, UUID profileId, UUID userId, TherapistInput input) {
        jobRepo.updateStatus(jobId, GenerationJobStatus.IN_PROGRESS);

        try {
            Map<SectionType, String> sections = aiService.generate(input);
            outputValidationService.validateOrThrow(sections);

            UUID pageId = pageRepo.insert(profileId, userId, sections, null);
            eventLog.log(EntityType.PAGE, pageId, EventType.GENERATED);

            jobRepo.markCompleted(jobId, pageId);
        } catch (Exception ex) {
            log.error("Generation job {} failed", jobId, ex);
            eventLog.log(
                    EntityType.PROFILE,
                    profileId,
                    EventType.GENERATION_FAILED,
                    JSONB.jsonb(toFailurePayload(ex))
            );
            jobRepo.markFailed(jobId, ex.getMessage());
        }
    }

    public GenerationJobResponse getJobStatus(UUID jobId) {
        GenerationJobRow job = jobRepo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        return toJobResponse(job);
    }

    public GeneratedPageResponse regenerateSection(UUID pageId, UUID userId, SectionType sectionType) {
        LandingPageRow page = pageRepo.findById(pageId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Page not found: " + pageId));

        TherapistProfileRow profile = profileRepo.findById(page.profileId())
                .orElseThrow(() -> new IllegalStateException("Profile not found for page: " + pageId));

        TherapistInput input = toInput(profile);
        String currentContent = page.sections().getOrDefault(sectionType, "");

        try {
            String regenerated = aiService.regenerateSection(input, sectionType, currentContent);

            // Merge the regenerated section into the full map and validate
            var updatedSections = new java.util.EnumMap<>(page.sections());
            updatedSections.put(sectionType, regenerated);
            outputValidationService.validateOrThrow(updatedSections);

            // Persist the single section
            pageRepo.updateSection(pageId, userId, sectionType, regenerated);
            eventLog.log(EntityType.PAGE, pageId, EventType.UPDATED);

            return new GeneratedPageResponse(
                    pageId,
                    page.profileId(),
                    profile.fullName(),
                    profile.role(),
                    structuredSectionsMapper.fromStorage(updatedSections),
                    page.status()
            );
        } catch (AiGenerationException | GenerationValidationException ex) {
            eventLog.log(
                    EntityType.PROFILE,
                    page.profileId(),
                    EventType.GENERATION_FAILED,
                    JSONB.jsonb(toFailurePayload(ex))
            );
            throw ex;
        }
    }

    private TherapistInput toInput(TherapistProfileRow profile) {
        return new TherapistInput(
                profile.fullName(),
                profile.role(),
                profile.location(),
                profile.audiences(),
                profile.areasOfSupport(),
                profile.approach(),
                profile.sessionFormat(),
                profile.expectations(),
                profile.contactMethod(),
                profile.contactValue()
        );
    }

    private String toFailurePayload(Exception ex) {
        String escapedMessage = ex.getMessage() == null ? "unknown" : ex.getMessage().replace("\"", "\\\"");
        return "{\"error\":\"" + escapedMessage + "\",\"type\":\"" + ex.getClass().getSimpleName() + "\"}";
    }

    private GenerationJobResponse toJobResponse(GenerationJobRow job) {
        return new GenerationJobResponse(
                job.id(),
                job.profileId(),
                job.pageId(),
                job.status(),
                job.error(),
                job.createdAt().toInstant(),
                job.updatedAt().toInstant()
        );
    }
}
