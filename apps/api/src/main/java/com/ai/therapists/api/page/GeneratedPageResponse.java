package com.ai.therapists.api.page;

import com.ai.therapists.api.profile.RoleType;
import com.ai.therapists.api.section_data.StructuredSections;

import java.util.UUID;

public record GeneratedPageResponse(
        UUID pageId,
        UUID profileId,
        String fullName,
        RoleType role,
        StructuredSections sections,
        PageStatus status
) {}
