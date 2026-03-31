package com.ai.therapists.api.generation;

import java.time.Instant;
import java.util.UUID;

public record GenerationJobResponse(
        UUID jobId,
        UUID profileId,
        UUID pageId,
        GenerationJobStatus status,
        String error,
        Instant createdAt,
        Instant updatedAt
) {}
