CREATE TABLE offer_status_history (
                                      id BIGSERIAL PRIMARY KEY,

                                      offer_id BIGINT NOT NULL,
                                      from_status VARCHAR(50),
                                      to_status VARCHAR(50) NOT NULL,

                                      changed_by BIGINT NOT NULL,
                                      changed_at TIMESTAMP NOT NULL,

                                      CONSTRAINT fk_history_offer
                                          FOREIGN KEY (offer_id)
                                              REFERENCES offers (id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_history_user
                                          FOREIGN KEY (changed_by)
                                              REFERENCES users (id)
);
