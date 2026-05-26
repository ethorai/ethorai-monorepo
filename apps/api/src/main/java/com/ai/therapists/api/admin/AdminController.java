package com.ai.therapists.api.admin;

import com.ai.therapists.api.page.GeneratedPageResponse;
import com.ai.therapists.api.page.LandingPageRepository;
import com.ai.therapists.api.page.StructuredSectionsMapper;
import com.ai.therapists.api.profile.TherapistProfileRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.ai.therapists.api.jooq.Tables.APP_USER;
import static com.ai.therapists.api.jooq.Tables.LANDING_PAGE;
import static com.ai.therapists.api.jooq.Tables.THERAPIST_PROFILE;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DSLContext dsl;
    private final LandingPageRepository pageRepo;
    private final TherapistProfileRepository profileRepo;
    private final StructuredSectionsMapper sectionsMapper;

    @GetMapping("/users")
    public List<AdminUserSummary> listUsers() {
        // Window function to pick the latest landing_page per user
        var latestPage = DSL.select(
                        LANDING_PAGE.USER_ID,
                        LANDING_PAGE.ID,
                        LANDING_PAGE.PROFILE_ID,
                        LANDING_PAGE.STATUS,
                        LANDING_PAGE.CREATED_AT,
                        DSL.rowNumber()
                                .over(DSL.partitionBy(LANDING_PAGE.USER_ID)
                                        .orderBy(LANDING_PAGE.CREATED_AT.desc()))
                                .as("rn"))
                .from(LANDING_PAGE)
                .asTable("lp");

        return dsl.select(
                        APP_USER.ID,
                        APP_USER.EMAIL,
                        APP_USER.NAME,
                        APP_USER.CREATED_AT,
                        latestPage.field("profile_id", UUID.class),
                        THERAPIST_PROFILE.FULL_NAME,
                        latestPage.field("id", UUID.class).as("page_id"),
                        latestPage.field("status", String.class).as("page_status"),
                        latestPage.field("created_at", OffsetDateTime.class).as("page_created_at"),
                        THERAPIST_PROFILE.SUBDOMAIN)
                .from(APP_USER)
                .leftJoin(latestPage)
                        .on(latestPage.field("user_id", UUID.class).eq(APP_USER.ID)
                                .and(latestPage.field("rn", Integer.class).eq(1)))
                .leftJoin(THERAPIST_PROFILE)
                        .on(THERAPIST_PROFILE.ID.eq(latestPage.field("profile_id", UUID.class)))
                .orderBy(APP_USER.CREATED_AT.desc())
                .fetch(r -> new AdminUserSummary(
                        r.get(APP_USER.ID),
                        r.get(APP_USER.EMAIL),
                        r.get(APP_USER.NAME),
                        r.get(APP_USER.CREATED_AT),
                        r.get(latestPage.field("profile_id", UUID.class)),
                        r.get(THERAPIST_PROFILE.FULL_NAME),
                        r.get(latestPage.field("id", UUID.class).as("page_id")),
                        r.get(latestPage.field("status", String.class).as("page_status")),
                        r.get(latestPage.field("created_at", OffsetDateTime.class).as("page_created_at")),
                        r.get(THERAPIST_PROFILE.SUBDOMAIN)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDetail> getUser(@PathVariable UUID userId) {
        var userRecord = dsl.selectFrom(APP_USER)
                .where(APP_USER.ID.eq(userId))
                .fetchOptional();

        if (userRecord.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var user = userRecord.get();

        GeneratedPageResponse pageResponse = pageRepo.findLatestByUserId(userId)
                .flatMap(page -> profileRepo.findById(page.profileId())
                        .map(profile -> new GeneratedPageResponse(
                                page.id(),
                                page.profileId(),
                                profile.fullName(),
                                profile.role(),
                                sectionsMapper.fromStorage(page.sections()),
                                page.status(),
                                profile.photoUrl(),
                                profile.city(),
                                profile.streetAddress(),
                                profile.postalCode(),
                                profile.latitude(),
                                profile.longitude(),
                                profile.sessionFormat().name(),
                                profile.subdomain())))
                .orElse(null);

        return ResponseEntity.ok(new AdminUserDetail(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCreatedAt(),
                pageResponse));
    }
}
