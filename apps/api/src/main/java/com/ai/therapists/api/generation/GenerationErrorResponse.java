package com.ai.therapists.api.generation;

import java.time.Instant;

public record GenerationErrorResponse(
        String code,
        String message,
        Instant timestamp
) {
}
