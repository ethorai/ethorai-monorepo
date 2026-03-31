package com.ai.therapists.api.generation;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.ai.therapists.api.jooq.Tables.GENERATION_JOB;

@Repository
@RequiredArgsConstructor
public class GenerationJobRepository {

    private final DSLContext dsl;

    public UUID insert(UUID profileId) {
        return dsl.insertInto(GENERATION_JOB)
                .set(GENERATION_JOB.PROFILE_ID, profileId)
                .set(GENERATION_JOB.STATUS, GenerationJobStatus.PENDING.name())
                .returning(GENERATION_JOB.ID)
                .fetchOne(GENERATION_JOB.ID);
    }

    public Optional<GenerationJobRow> findById(UUID id) {
        return dsl.selectFrom(GENERATION_JOB)
                .where(GENERATION_JOB.ID.eq(id))
                .fetchOptional(r -> new GenerationJobRow(
                        r.get(GENERATION_JOB.ID),
                        r.get(GENERATION_JOB.PROFILE_ID),
                        r.get(GENERATION_JOB.PAGE_ID),
                        GenerationJobStatus.valueOf(r.get(GENERATION_JOB.STATUS)),
                        r.get(GENERATION_JOB.ERROR),
                        r.get(GENERATION_JOB.CREATED_AT),
                        r.get(GENERATION_JOB.UPDATED_AT)
                ));
    }

    public void updateStatus(UUID jobId, GenerationJobStatus status) {
        dsl.update(GENERATION_JOB)
                .set(GENERATION_JOB.STATUS, status.name())
                .set(GENERATION_JOB.UPDATED_AT, OffsetDateTime.now())
                .where(GENERATION_JOB.ID.eq(jobId))
                .execute();
    }

    public void markCompleted(UUID jobId, UUID pageId) {
        dsl.update(GENERATION_JOB)
                .set(GENERATION_JOB.STATUS, GenerationJobStatus.COMPLETED.name())
                .set(GENERATION_JOB.PAGE_ID, pageId)
                .set(GENERATION_JOB.UPDATED_AT, OffsetDateTime.now())
                .where(GENERATION_JOB.ID.eq(jobId))
                .execute();
    }

    public void markFailed(UUID jobId, String error) {
        dsl.update(GENERATION_JOB)
                .set(GENERATION_JOB.STATUS, GenerationJobStatus.FAILED.name())
                .set(GENERATION_JOB.ERROR, error)
                .set(GENERATION_JOB.UPDATED_AT, OffsetDateTime.now())
                .where(GENERATION_JOB.ID.eq(jobId))
                .execute();
    }

    public record GenerationJobRow(
            UUID id,
            UUID profileId,
            UUID pageId,
            GenerationJobStatus status,
            String error,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {}
}
