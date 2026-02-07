-- ============================================
-- V10__offer_created_at.sql
-- ============================================

ALTER TABLE offers
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
