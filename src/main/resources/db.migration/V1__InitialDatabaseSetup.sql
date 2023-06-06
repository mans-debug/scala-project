CREATE TABLE account
(
    id            BIGSERIAL PRIMARY KEY,
    login         VARCHAR(30) UNIQUE,
    password_hash VARCHAR(60)
);

CREATE TABLE access_token
(
    id      BIGSERIAL,
    value   VARCHAR(64),
    user_id BIGINT REFERENCES account (id)
);
