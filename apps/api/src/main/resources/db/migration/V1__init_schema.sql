-- ============================================================
-- V1 — Initial schema: therapist_profile, landing_page, event_log
-- ============================================================

CREATE TABLE therapist_profile (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name        VARCHAR(255) NOT NULL,
    role             VARCHAR(50)  NOT NULL,
    location         VARCHAR(255),
    audiences        JSONB        NOT NULL DEFAULT '[]'::jsonb,
    areas_of_support JSONB        NOT NULL DEFAULT '[]'::jsonb,
    approach         TEXT,
    session_format   VARCHAR(20)  NOT NULL,
    expectations     JSONB        NOT NULL DEFAULT '[]'::jsonb,
    contact_method   VARCHAR(20)  NOT NULL,
    contact_value    VARCHAR(500) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE landing_page (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id     UUID        NOT NULL REFERENCES therapist_profile(id),
    sections       JSONB       NOT NULL DEFAULT '{}'::jsonb,
    status         VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    generation_log JSONB,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_landing_page_profile ON landing_page(profile_id);

CREATE TABLE event_log (
    id          BIGSERIAL   PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id   UUID        NOT NULL,
    event_type  VARCHAR(50) NOT NULL,
    payload     JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_event_log_entity ON event_log(entity_type, entity_id);
