-- ============================================
-- V9__offer_customer_token.sql
-- PostgreSQL
-- ============================================

ALTER TABLE offers
    ADD COLUMN customer_token VARCHAR(64);

-- doplnění tokenu pro existující záznamy
UPDATE offers
SET customer_token = gen_random_uuid()::text
WHERE customer_token IS NULL;

ALTER TABLE offers
    ALTER COLUMN customer_token SET NOT NULL;

CREATE UNIQUE INDEX idx_offers_customer_token
    ON offers(customer_token);
