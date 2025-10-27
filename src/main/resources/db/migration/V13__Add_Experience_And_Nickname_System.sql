-- Adaugă noile coloane la tabela de utilizatori
ALTER TABLE users
    ADD COLUMN experience BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN xp_rate DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    ADD COLUMN nickname VARCHAR(50) UNIQUE;

-- Creează tabela pentru istoricul de experiență
CREATE TABLE xp_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount INT NOT NULL,
    source_type VARCHAR(50) NOT NULL, -- ex: 'SHOP_PURCHASE', 'RECEIPT_CLAIM'
    description TEXT,
    created_at TIMESTAMP,
    CONSTRAINT fk_xp_transaction_user FOREIGN KEY(user_id) REFERENCES users(id)
);