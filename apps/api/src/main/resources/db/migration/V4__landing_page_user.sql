-- ============================================================
-- V4 — Add user_id FK to landing_page
-- Nullable: existing rows belong to no user yet.
-- Will be enforced NOT NULL after auth is wired end-to-end.
-- ============================================================

ALTER TABLE landing_page
    ADD COLUMN user_id UUID REFERENCES app_user(id) ON DELETE SET NULL;

CREATE INDEX idx_landing_page_user ON landing_page(user_id);
