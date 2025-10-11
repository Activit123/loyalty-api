-- V7__Update_Products_For_Shop_And_Rewards.sql

-- Ștergem vechea coloană de cost, deoarece o vom înlocui
ALTER TABLE products DROP COLUMN cost_in_coins;

-- Adăugăm noile coloane pentru prețuri și stoc
ALTER TABLE products
    ADD COLUMN buy_price INT NOT NULL DEFAULT 0,  -- Prețul de cumpărare pentru client
    ADD COLUMN claim_value INT NOT NULL DEFAULT 0, -- Valoarea în monede acordată de admin
    ADD COLUMN stock INT DEFAULT -1;              -- Cantitatea în stoc (-1 pentru nelimitat)

-- Adăugăm un comentariu pentru claritate
COMMENT ON COLUMN products.stock IS 'Reprezintă cantitatea disponibilă. -1 înseamnă stoc nelimitat.';