package com.ai.therapists.api.page;

import com.ai.therapists.api.event.EntityType;
import com.ai.therapists.api.event.EventLogRepository;
import com.ai.therapists.api.event.EventType;
import com.ai.therapists.api.generation.GenerationOrchestrator;
import com.ai.therapists.api.page.LandingPageRepository.LandingPageRow;
import com.ai.therapists.api.profile.TherapistProfileRepository;
import com.ai.therapists.api.profile.TherapistProfileRepository.TherapistProfileRow;
import com.ai.therapists.api.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {

    private final LandingPageRepository pageRepo;
    private final TherapistProfileRepository profileRepo;
    private final EventLogRepository eventLog;
    private final StructuredSectionsMapper structuredSectionsMapper;
    private final GenerationOrchestrator generationOrchestrator;

    @GetMapping("/{id}")
    public ResponseEntity<GeneratedPageResponse> getPage(@PathVariable UUID id) {
        UUID userId = SecurityContextHelper.currentUserId();
        return pageRepo.findById(id, userId)
                .flatMap(page -> profileRepo.findById(page.profileId())
                        .map(profile -> toResponse(page, profile)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<GeneratedPageResponse>> getPagesByProfile(@RequestParam UUID profileId) {
        UUID userId = SecurityContextHelper.currentUserId();
        var profile = profileRepo.findById(profileId);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var pages = pageRepo.findByProfileId(profileId, userId).stream()
                .map(page -> toResponse(page, profile.get()))
                .toList();

        return ResponseEntity.ok(pages);
    }

    @PutMapping("/{id}/sections/{sectionType}")
    public ResponseEntity<Void> updateSection(@PathVariable UUID id,
                                              @PathVariable SectionType sectionType,
                                              @RequestBody String content) {
        UUID userId = SecurityContextHelper.currentUserId();
        if (pageRepo.findById(id, userId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        pageRepo.updateSection(id, userId, sectionType, content);
        eventLog.log(EntityType.PAGE, id, EventType.UPDATED);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable UUID id) {
        UUID userId = SecurityContextHelper.currentUserId();
        if (pageRepo.findById(id, userId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        pageRepo.updateStatus(id, userId, PageStatus.PUBLISHED);
        eventLog.log(EntityType.PAGE, id, EventType.PUBLISHED);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/sections/{sectionType}/regenerate")
    public ResponseEntity<GeneratedPageResponse> regenerateSection(@PathVariable UUID id,
                                                                    @PathVariable SectionType sectionType) {
        UUID userId = SecurityContextHelper.currentUserId();
        if (pageRepo.findById(id, userId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GeneratedPageResponse response = generationOrchestrator.regenerateSection(id, userId, sectionType);
        return ResponseEntity.ok(response);
    }

    private GeneratedPageResponse toResponse(LandingPageRow page, TherapistProfileRow profile) {
        return new GeneratedPageResponse(
                page.id(),
                page.profileId(),
                profile.fullName(),
                profile.role(),
                structuredSectionsMapper.fromStorage(page.sections()),
                page.status()
        );
    }
}
