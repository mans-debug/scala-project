CREATE TABLE notebook
(
    id BIGSERIAL PRIMARY KEY,
    book_name VARCHAR(30),
    book_owner BIGINT REFERENCES account (id)
);

CREATE TABLE note
(
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(30),
    notebook_id BIGINT REFERENCES notebook (id) ON DELETE CASCADE,
    note_content TEXT
);

CREATE TABLE roles
(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES account (id),
    notebook_id BIGINT REFERENCES notebook (id) ON DELETE CASCADE,
    user_role VARCHAR(5),
    UNIQUE (user_id, notebook_id)
);
