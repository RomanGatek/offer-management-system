ALTER TABLE offers
    ADD COLUMN token_expires_at TIMESTAMP;

UPDATE offers
SET token_expires_at = created_at + INTERVAL '30 days';

ALTER TABLE offers
    ALTER COLUMN token_expires_at SET NOT NULL;
