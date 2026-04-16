package com.ai.therapists.api.generation;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GenerationRateLimiterTest {

    private final GenerationRateLimiter rateLimiter = new GenerationRateLimiter();

    @Test
    void consume_allowsUpToLimit() {
        UUID userId = UUID.randomUUID();
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiter.consume(userId));
        }
    }

    @Test
    void consume_throwsOnSixthRequest() {
        UUID userId = UUID.randomUUID();
        for (int i = 0; i < 5; i++) {
            rateLimiter.consume(userId);
        }
        assertThrows(RateLimitExceededException.class, () -> rateLimiter.consume(userId));
    }

    @Test
    void consume_bucketsAreIndependentPerUser() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        for (int i = 0; i < 5; i++) {
            rateLimiter.consume(user1);
        }
        // user1 is exhausted — user2 should still have a full bucket
        assertDoesNotThrow(() -> rateLimiter.consume(user2));
    }
}
