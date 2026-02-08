CREATE TABLE offer_access_log (
                                  id BIGSERIAL PRIMARY KEY,

                                  offer_id BIGINT NOT NULL,
                                  accessed_at TIMESTAMP NOT NULL,
                                  ip_address VARCHAR(64),
                                  user_agent TEXT,

                                  CONSTRAINT fk_offer_access_log_offer
                                      FOREIGN KEY (offer_id)
                                          REFERENCES offers (id)
                                          ON DELETE CASCADE
);
