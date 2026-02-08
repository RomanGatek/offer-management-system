CREATE TABLE audit_log (
                           id BIGSERIAL PRIMARY KEY,
                           offer_id BIGINT NULL,
                           user_id BIGINT NULL,
                           action VARCHAR(60) NOT NULL,
                           performed_at TIMESTAMP NOT NULL DEFAULT NOW(),
                           detail TEXT NULL,

                           CONSTRAINT fk_audit_offer
                               FOREIGN KEY (offer_id)
                                   REFERENCES offers(id)
                                   ON DELETE SET NULL,

                           CONSTRAINT fk_audit_user
                               FOREIGN KEY (user_id)
                                   REFERENCES users(id)
                                   ON DELETE SET NULL
);

CREATE INDEX idx_audit_offer_id ON audit_log(offer_id);
CREATE INDEX idx_audit_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_performed_at ON audit_log(performed_at);
