-- ============================================================
-- V2 — Generation job table for async generation tracking
-- ============================================================

CREATE TABLE generation_job (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id  UUID        NOT NULL REFERENCES therapist_profile(id),
    page_id     UUID        REFERENCES landing_page(id),
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error       TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_generation_job_profile ON generation_job(profile_id);
CREATE INDEX idx_generation_job_status ON generation_job(status);
