package com.ai.therapists.api.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminUserSummary(
        UUID userId,
        String email,
        String name,
        OffsetDateTime createdAt,
        UUID profileId,
        String fullName,
        UUID pageId,
        String pageStatus,
        OffsetDateTime pageCreatedAt,
        String subdomain
) {}
