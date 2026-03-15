package com.ai.therapists.api.page;

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

    public UUID insert(UUID profileId, Map<SectionType, String> sections, JSONB generationLog) {
        return dsl.insertInto(LANDING_PAGE)
                .set(LANDING_PAGE.PROFILE_ID, profileId)
                .set(LANDING_PAGE.SECTIONS, JSONB.jsonb(sectionsToJson(sections)))
                .set(LANDING_PAGE.STATUS, PageStatus.DRAFT.name())
                .set(LANDING_PAGE.GENERATION_LOG, generationLog)
                .returning(LANDING_PAGE.ID)
                .fetchOne(LANDING_PAGE.ID);
    }

    public Optional<LandingPageRow> findById(UUID id) {
        return dsl.selectFrom(LANDING_PAGE)
                .where(LANDING_PAGE.ID.eq(id))
                .fetchOptional(this::toRow);
    }

    public List<LandingPageRow> findByProfileId(UUID profileId) {
        return dsl.selectFrom(LANDING_PAGE)
                .where(LANDING_PAGE.PROFILE_ID.eq(profileId))
                .orderBy(LANDING_PAGE.CREATED_AT.desc())
                .fetch(this::toRow);
    }

    public void updateSection(UUID pageId, SectionType sectionType, String content) {
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
                .execute();
    }

    public void updateStatus(UUID pageId, PageStatus status) {
        dsl.update(LANDING_PAGE)
                .set(LANDING_PAGE.STATUS, status.name())
                .set(LANDING_PAGE.UPDATED_AT, OffsetDateTime.now())
                .where(LANDING_PAGE.ID.eq(pageId))
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
        if (sections == null || sections.isEmpty()) return "{}";
        return "{" + sections.entrySet().stream()
                .map(e -> "\"" + e.getKey().name() + "\":\"" + e.getValue().replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(",")) + "}";
    }

    private Map<SectionType, String> sectionsFromJson(JSONB jsonb) {
        if (jsonb == null || jsonb.data() == null || jsonb.data().equals("{}")) {
            return new EnumMap<>(SectionType.class);
        }
        // Simple parsing: {"KEY":"value","KEY2":"value2"}
        Map<SectionType, String> result = new EnumMap<>(SectionType.class);
        String data = jsonb.data();
        data = data.substring(1, data.length() - 1); // remove { }
        if (data.isEmpty()) return result;

        // Split on "," that are between entries (not inside values)
        // Simple approach: split on `","` pattern between key-value pairs
        String[] pairs = data.split(",(?=\"[A-Z])");
        for (String pair : pairs) {
            int colonIdx = pair.indexOf(":");
            if (colonIdx < 0) continue;
            String key = pair.substring(0, colonIdx).trim().replaceAll("^\"|\"$", "");
            String value = pair.substring(colonIdx + 1).trim().replaceAll("^\"|\"$", "");
            try {
                result.put(SectionType.valueOf(key), value.replace("\\\"", "\""));
            } catch (IllegalArgumentException ignored) {
                // Skip unknown section types
            }
        }
        return result;
    }
}
