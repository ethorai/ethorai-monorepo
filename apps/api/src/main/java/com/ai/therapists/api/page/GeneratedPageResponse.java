package com.ai.therapists.api.page;

import com.ai.therapists.api.profile.RoleType;

import java.util.Map;
import java.util.UUID;

public record GeneratedPageResponse(
        UUID pageId,
        UUID profileId,
        String fullName,
        RoleType role,
        Map<SectionType, String> sections,
        PageStatus status
) {}
