-- V31__Add_InitiatorId_To_Friendship.sql

-- PASUL 1: Adaugă coloana Initiator_ID (permite NULL)
ALTER TABLE friendships
    ADD COLUMN initiator_id BIGINT;

-- PASUL 2: Populează coloana pentru rândurile existente (CRUCIAL!)
-- Presupunem că în schema veche, cel cu ID-ul mai mic (user_a_id) a inițiat cererea.
UPDATE friendships
    SET initiator_id = user_a_id
    WHERE initiator_id IS NULL; -- Asigură-te că nu suprascrie nicio valoare nouă

-- PASUL 3: Adaugă constrângerea NOT NULL
ALTER TABLE friendships
    ALTER COLUMN initiator_id SET NOT NULL;

-- PASUL 4: Adaugă cheia externă (cel mai bine este să o adaugi după ce ai stabilit NOT NULL)
ALTER TABLE friendships
    ADD CONSTRAINT fk_friendship_initiator FOREIGN KEY (initiator_id) REFERENCES users(id);