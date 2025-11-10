-- V18__Create_BoardGames_Table.sql
CREATE TABLE board_games (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(255),
    players VARCHAR(50),      -- Ex: 2-4 jucatori
    play_time VARCHAR(50),    -- Ex: 60-90 min
    age_limit VARCHAR(50),    -- Ex: 12+
    complexity_rating DOUBLE PRECISION, -- Optional: 1.0 - 5.0
    category VARCHAR(50) NOT NULL, -- Initiati, Maestri, Legende
    created_at TIMESTAMP
);