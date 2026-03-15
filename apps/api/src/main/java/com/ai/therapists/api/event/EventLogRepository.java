package com.ai.therapists.api.event;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static com.ai.therapists.api.jooq.Tables.EVENT_LOG;

@Repository
@RequiredArgsConstructor
public class EventLogRepository {

    private final DSLContext dsl;

    public void log(EntityType entityType, UUID entityId, EventType eventType, JSONB payload) {
        dsl.insertInto(EVENT_LOG)
                .set(EVENT_LOG.ENTITY_TYPE, entityType.name())
                .set(EVENT_LOG.ENTITY_ID, entityId)
                .set(EVENT_LOG.EVENT_TYPE, eventType.name())
                .set(EVENT_LOG.PAYLOAD, payload)
                .execute();
    }

    public void log(EntityType entityType, UUID entityId, EventType eventType) {
        log(entityType, entityId, eventType, null);
    }
}
