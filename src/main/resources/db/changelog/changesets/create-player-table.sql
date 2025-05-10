CREATE TABLE IF NOT EXISTS player (
    id BIGSERIAL PRIMARY KEY,
    vk_id BIGINT UNIQUE NOT NULL,
    first_name TEXT,
    last_name TEXT,
    birth_year INT
);