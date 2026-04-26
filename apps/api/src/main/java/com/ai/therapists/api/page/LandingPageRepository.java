package com.ai.therapists.api.page;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ai.therapists.api.jooq.Tables.LANDING_PAGE;

@Repository
@RequiredArgsConstructor
public class LandingPageRepository {

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public UUID insert(UUID profileId, UUID userId, Map<SectionType, String> sections, JSONB generationLog) {
        return dsl.insertInto(LANDING_PAGE)
                .set(LANDING_PAGE.PROFILE_ID, profileId)
                .set(LANDING_PAGE.USER_ID, userId)
                .set(LANDING_PAGE.SECTIONS, JSONB.jsonb(sectionsToJson(sections)))
                .set(LANDING_PAGE.STATUS, PageStatus.DRAFT.name())
                .set(LANDING_PAGE.GENERATION_LOG, generationLog)
                .returning(LANDING_PAGE.ID)
                .fetchOne(LANDING_PAGE.ID);
    }

    public Optional<LandingPageRow> findById(UUID id, UUID userId) {
        return dsl.selectFrom(LANDING_PAGE)
                .where(LANDING_PAGE.ID.eq(id))
                .and(LANDING_PAGE.USER_ID.eq(userId))
                .fetchOptional(this::toRow);
    }

    public Optional<LandingPageRow> findByIdPublic(UUID id) {
        return dsl.selectFrom(LANDING_PAGE)
                .where(LANDING_PAGE.ID.eq(id))
                .and(LANDING_PAGE.STATUS.eq(PageStatus.PUBLISHED.name()))
                .fetchOptional(this::toRow);
    }

    public List<LandingPageRow> findByProfileId(UUID profileId, UUID userId) {
        return dsl.selectFrom(LANDING_PAGE)
                .where(LANDING_PAGE.PROFILE_ID.eq(profileId))
                .and(LANDING_PAGE.USER_ID.eq(userId))
                .orderBy(LANDING_PAGE.CREATED_AT.desc())
                .fetch(this::toRow);
    }

    public Optional<LandingPageRow> findLatestByUserId(UUID userId) {
        return dsl.selectFrom(LANDING_PAGE)
                .where(LANDING_PAGE.USER_ID.eq(userId))
                .orderBy(LANDING_PAGE.CREATED_AT.desc())
                .limit(1)
                .fetchOptional(this::toRow);
    }

    public void updateSection(UUID pageId, UUID userId, SectionType sectionType, String content) {
        // Use PostgreSQL jsonb_set to update a single key
        dsl.update(LANDING_PAGE)
                .set(LANDING_PAGE.SECTIONS,
                        org.jooq.impl.DSL.field(
                                "jsonb_set(sections, {0}, {1})",
                                JSONB.class,
                                org.jooq.impl.DSL.val("{" + sectionType.name() + "}"),
                                org.jooq.impl.DSL.val(JSONB.jsonb("\"" + content.replace("\"", "\\\"") + "\""))
                        ))
                .set(LANDING_PAGE.UPDATED_AT, OffsetDateTime.now())
                .where(LANDING_PAGE.ID.eq(pageId))
                .and(LANDING_PAGE.USER_ID.eq(userId))
                .execute();
    }

    public void updateStatus(UUID pageId, UUID userId, PageStatus status) {
        dsl.update(LANDING_PAGE)
                .set(LANDING_PAGE.STATUS, status.name())
                .set(LANDING_PAGE.UPDATED_AT, OffsetDateTime.now())
                .where(LANDING_PAGE.ID.eq(pageId))
                .and(LANDING_PAGE.USER_ID.eq(userId))
                .execute();
    }

    public record LandingPageRow(
            UUID id,
            UUID profileId,
            Map<SectionType, String> sections,
            PageStatus status,
            JSONB generationLog,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {}

    private LandingPageRow toRow(org.jooq.Record record) {
        return new LandingPageRow(
                record.get(LANDING_PAGE.ID),
                record.get(LANDING_PAGE.PROFILE_ID),
                sectionsFromJson(record.get(LANDING_PAGE.SECTIONS)),
                PageStatus.valueOf(record.get(LANDING_PAGE.STATUS)),
                record.get(LANDING_PAGE.GENERATION_LOG),
                record.get(LANDING_PAGE.CREATED_AT),
                record.get(LANDING_PAGE.UPDATED_AT)
        );
    }

    private String sectionsToJson(Map<SectionType, String> sections) {
        if (sections == null || sections.isEmpty()) {
            return "{}";
        }

        try {
            return objectMapper.writeValueAsString(sections);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize landing page sections", ex);
        }
    }

    private Map<SectionType, String> sectionsFromJson(JSONB jsonb) {
        if (jsonb == null || jsonb.data() == null || jsonb.data().equals("{}")) {
            return new EnumMap<>(SectionType.class);
        }
        try {
            Map<String, String> rawSections = objectMapper.readValue(
                    jsonb.data(),
                    new TypeReference<Map<String, String>>() {
                    }
            );

            Map<SectionType, String> result = new EnumMap<>(SectionType.class);
            rawSections.forEach((key, value) -> {
                try {
                    result.put(SectionType.valueOf(key), value);
                } catch (IllegalArgumentException ignored) {
                    // Skip unknown section types
                }
            });
            return result;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to deserialize landing page sections", ex);
        }
    }
}
