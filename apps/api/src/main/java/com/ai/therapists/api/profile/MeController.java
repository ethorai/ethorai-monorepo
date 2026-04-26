package com.ai.therapists.api.profile;

import com.ai.therapists.api.page.GeneratedPageResponse;
import com.ai.therapists.api.page.LandingPageRepository;
import com.ai.therapists.api.page.LandingPageRepository.LandingPageRow;
import com.ai.therapists.api.page.StructuredSectionsMapper;
import com.ai.therapists.api.profile.TherapistProfileRepository.TherapistProfileRow;
import com.ai.therapists.api.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// Singleton-shaped endpoints for the current authenticated user.
// Returns 204 No Content when the user has no page/profile yet.
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final LandingPageRepository pageRepo;
    private final TherapistProfileRepository profileRepo;
    private final StructuredSectionsMapper structuredSectionsMapper;

    @GetMapping("/page")
    public ResponseEntity<GeneratedPageResponse> getMyPage() {
        UUID userId = SecurityContextHelper.currentUserId();
        return pageRepo.findLatestByUserId(userId)
                .flatMap(page -> profileRepo.findById(page.profileId())
                        .map(profile -> toResponse(page, profile)))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/profile")
    public ResponseEntity<TherapistInput> getMyProfile() {
        UUID userId = SecurityContextHelper.currentUserId();
        return pageRepo.findLatestByUserId(userId)
                .flatMap(page -> profileRepo.findById(page.profileId()))
                .map(this::toInput)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
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
}
