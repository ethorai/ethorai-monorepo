package com.ai.therapists.api.profile;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ai.therapists.api.jooq.Tables.THERAPIST_PROFILE;

@Repository
@RequiredArgsConstructor
public class TherapistProfileRepository {

    private final DSLContext dsl;

    public UUID insert(String fullName,
                       RoleType role,
                       String location,
                       List<String> audiences,
                       List<String> areasOfSupport,
                       String approach,
                       SessionFormat sessionFormat,
                       List<String> expectations,
                       ContactMethod contactMethod,
                       String contactValue) {

        return dsl.insertInto(THERAPIST_PROFILE)
                .set(THERAPIST_PROFILE.FULL_NAME, fullName)
                .set(THERAPIST_PROFILE.ROLE, role.name())
                .set(THERAPIST_PROFILE.LOCATION, location)
                .set(THERAPIST_PROFILE.AUDIENCES, JSONB.jsonb(toJson(audiences)))
                .set(THERAPIST_PROFILE.AREAS_OF_SUPPORT, JSONB.jsonb(toJson(areasOfSupport)))
                .set(THERAPIST_PROFILE.APPROACH, approach)
                .set(THERAPIST_PROFILE.SESSION_FORMAT, sessionFormat.name())
                .set(THERAPIST_PROFILE.EXPECTATIONS, JSONB.jsonb(toJson(expectations)))
                .set(THERAPIST_PROFILE.CONTACT_METHOD, contactMethod.name())
                .set(THERAPIST_PROFILE.CONTACT_VALUE, contactValue)
                .returning(THERAPIST_PROFILE.ID)
                .fetchOne(THERAPIST_PROFILE.ID);
    }

    public Optional<TherapistProfileRow> findById(UUID id) {
        return dsl.selectFrom(THERAPIST_PROFILE)
                .where(THERAPIST_PROFILE.ID.eq(id))
                .fetchOptional(this::toRow);
    }

    public record TherapistProfileRow(
            UUID id,
            String fullName,
            RoleType role,
            String location,
            List<String> audiences,
            List<String> areasOfSupport,
            String approach,
            SessionFormat sessionFormat,
            List<String> expectations,
            ContactMethod contactMethod,
            String contactValue
    ) {}

    private TherapistProfileRow toRow(org.jooq.Record record) {
        return new TherapistProfileRow(
                record.get(THERAPIST_PROFILE.ID),
                record.get(THERAPIST_PROFILE.FULL_NAME),
                RoleType.valueOf(record.get(THERAPIST_PROFILE.ROLE)),
                record.get(THERAPIST_PROFILE.LOCATION),
                fromJson(record.get(THERAPIST_PROFILE.AUDIENCES)),
                fromJson(record.get(THERAPIST_PROFILE.AREAS_OF_SUPPORT)),
                record.get(THERAPIST_PROFILE.APPROACH),
                SessionFormat.valueOf(record.get(THERAPIST_PROFILE.SESSION_FORMAT)),
                fromJson(record.get(THERAPIST_PROFILE.EXPECTATIONS)),
                ContactMethod.valueOf(record.get(THERAPIST_PROFILE.CONTACT_METHOD)),
                record.get(THERAPIST_PROFILE.CONTACT_VALUE)
        );
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        return "[" + String.join(",", list.stream().map(s -> "\"" + s.replace("\"", "\\\"") + "\"").toList()) + "]";
    }

    @SuppressWarnings("unchecked")
    private List<String> fromJson(JSONB jsonb) {
        if (jsonb == null || jsonb.data() == null || jsonb.data().equals("[]")) {
            return List.of();
        }
        String data = jsonb.data();
        // Simple JSON array parsing — elements are quoted strings
        return List.of(
                data.substring(1, data.length() - 1)
                        .split(",")
        ).stream()
                .map(s -> s.trim().replaceAll("^\"|\"$", ""))
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
