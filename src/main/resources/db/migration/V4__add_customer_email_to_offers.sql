ALTER TABLE offers
    ADD COLUMN customer_email VARCHAR(255) NOT NULL DEFAULT 'unknown@example.com';