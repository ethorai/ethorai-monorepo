package com.ai.therapists.api.admin;

import com.ai.therapists.api.page.GeneratedPageResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminUserDetail(
        UUID userId,
        String email,
        String name,
        OffsetDateTime createdAt,
        GeneratedPageResponse page
) {}
