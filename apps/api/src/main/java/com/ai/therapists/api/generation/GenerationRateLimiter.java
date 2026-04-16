package com.ai.therapists.api.generation;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GenerationRateLimiter {

    private static final int MAX_REQUESTS_PER_HOUR = 5;

    private final ConcurrentHashMap<UUID, Bucket> buckets = new ConcurrentHashMap<>();

    public void consume(UUID userId) {
        Bucket bucket = buckets.computeIfAbsent(userId, this::newBucket);
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException();
        }
    }

    private Bucket newBucket(UUID ignored) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(MAX_REQUESTS_PER_HOUR)
                        .refillGreedy(MAX_REQUESTS_PER_HOUR, Duration.ofHours(1))
                        .build())
                .build();
    }
}
