package com.ai.therapists.api.generation;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException() {
        super("Generation rate limit exceeded. Maximum 5 requests per hour.");
    }
}
