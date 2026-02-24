CREATE TABLE tournaments (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    game_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    max_players INTEGER NOT NULL,
    entry_fee_coins INTEGER DEFAULT 0,
    prize_description VARCHAR(255),
    prize_coins INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'REGISTRATION_OPEN', -- REGISTRATION_OPEN, ONGOING, COMPLETED, CANCELED
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE tournament_participants (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_t_part_t FOREIGN KEY (tournament_id) REFERENCES tournaments(id),
    CONSTRAINT fk_t_part_u FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uc_user_tourn UNIQUE (tournament_id, user_id)
);

CREATE TABLE tournament_matches (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    player1_id BIGINT,
    player2_id BIGINT,
    winner_id BIGINT,
    round_number INTEGER NOT NULL,
    match_order INTEGER NOT NULL,
    next_match_id BIGINT,
    CONSTRAINT fk_tm_t FOREIGN KEY (tournament_id) REFERENCES tournaments(id),
    CONSTRAINT fk_tm_p1 FOREIGN KEY (player1_id) REFERENCES users(id),
    CONSTRAINT fk_tm_p2 FOREIGN KEY (player2_id) REFERENCES users(id),
    CONSTRAINT fk_tm_w FOREIGN KEY (winner_id) REFERENCES users(id)
);