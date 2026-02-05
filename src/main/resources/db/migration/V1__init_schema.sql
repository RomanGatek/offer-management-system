CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL
);

CREATE TABLE offers (
                        id BIGSERIAL PRIMARY KEY,
                        customer_name VARCHAR(255) NOT NULL,
                        description VARCHAR(255) NOT NULL,
                        total_price NUMERIC(38,2) NOT NULL,
                        created_date DATE,
                        status VARCHAR(50),
                        user_id BIGINT NOT NULL,
                        CONSTRAINT fk_offer_user
                            FOREIGN KEY (user_id)
                                REFERENCES users(id)
);