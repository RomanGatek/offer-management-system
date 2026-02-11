CREATE TABLE audit_log (
                           id BIGSERIAL PRIMARY KEY,

                           offer_id BIGINT NULL,
                           performed_by BIGINT NULL,

                           action VARCHAR(60) NOT NULL,
                           section VARCHAR(100) NULL,

                           detail TEXT NULL,
                           old_value TEXT NULL,
                           new_value TEXT NULL,

                           ip_address VARCHAR(100) NULL,
                           user_agent TEXT NULL,

                           performed_at TIMESTAMP NOT NULL DEFAULT NOW(),

                           CONSTRAINT fk_audit_offer
                               FOREIGN KEY (offer_id)
                                   REFERENCES offers(id)
                                   ON DELETE SET NULL,

                           CONSTRAINT fk_audit_user
                               FOREIGN KEY (performed_by)
                                   REFERENCES users(id)
                                   ON DELETE SET NULL
);

CREATE INDEX idx_audit_offer_id ON audit_log(offer_id);
CREATE INDEX idx_audit_performed_by ON audit_log(performed_by);
CREATE INDEX idx_audit_performed_at ON audit_log(performed_at);
