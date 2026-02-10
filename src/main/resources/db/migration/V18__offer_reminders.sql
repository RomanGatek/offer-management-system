ALTER TABLE offers
    ADD COLUMN first_reminder_sent_at TIMESTAMP;

ALTER TABLE offers
    ADD COLUMN second_reminder_sent_at TIMESTAMP;
