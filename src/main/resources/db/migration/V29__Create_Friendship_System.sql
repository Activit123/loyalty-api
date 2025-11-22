-- V24__Create_Friendship_System.sql

CREATE TABLE friendships (
    id BIGSERIAL PRIMARY KEY,
    user_a_id BIGINT NOT NULL,
    user_b_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),

    -- Note: user_a_id trebuie să fie întotdeauna ID-ul mai mic
    -- Logica de a menține ordinea canonică este în Service Layer
    CONSTRAINT fk_friendship_user_a FOREIGN KEY (user_a_id) REFERENCES users(id),
    CONSTRAINT fk_friendship_user_b FOREIGN KEY (user_b_id) REFERENCES users(id),

    -- Nu permite cereri duble între aceiași doi utilizatori (ordine specifică)
    CONSTRAINT uc_friendships_users UNIQUE (user_a_id, user_b_id),

    -- Nu permite prietenia cu tine însuți (deși e prevenit și în Service)
    CONSTRAINT chk_different_users CHECK (user_a_id <> user_b_id)
);