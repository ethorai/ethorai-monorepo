package com.ai.therapists.api.page;

import com.ai.therapists.api.page.LandingPageRepository.LandingPageRow;
import com.ai.therapists.api.profile.TherapistProfileRepository;
import com.ai.therapists.api.profile.TherapistProfileRepository.TherapistProfileRow;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicPageController {

    private final LandingPageRepository pageRepo;
    private final TherapistProfileRepository profileRepo;
    private final StructuredSectionsMapper sectionsMapper;

    @GetMapping("/pages/{id}")
    public ResponseEntity<GeneratedPageResponse> getPublishedPage(@PathVariable UUID id) {
        return pageRepo.findByIdPublic(id)
                .flatMap(page -> profileRepo.findById(page.profileId())
                        .map(profile -> toResponse(page, profile)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private GeneratedPageResponse toResponse(LandingPageRow page, TherapistProfileRow profile) {
        return new GeneratedPageResponse(
                page.id(),
                page.profileId(),
                profile.fullName(),
                profile.role(),
                sectionsMapper.fromStorage(page.sections()),
                page.status(),
                profile.photoUrl()
        );
    }
}
