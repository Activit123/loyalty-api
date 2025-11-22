-- V33__Create_Trade_System_Tables.sql

-- Tabela 1: Trades (Tranzacțiile Principale)
CREATE TABLE trades (
    id BIGSERIAL PRIMARY KEY,
    initiator_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    initiator_agreed BOOLEAN NOT NULL DEFAULT FALSE,
    recipient_agreed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_trade_initiator FOREIGN KEY (initiator_id) REFERENCES users(id),
    CONSTRAINT fk_trade_recipient FOREIGN KEY (recipient_id) REFERENCES users(id),
    CONSTRAINT chk_trade_different_users CHECK (initiator_id <> recipient_id)
);

-- Tabela 2: Trade Offer Items (Itemele și Monedele Oferite)
CREATE TABLE trade_offer_items (
    id BIGSERIAL PRIMARY KEY,
    trade_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    offered_amount INTEGER, -- Suma de monede (dacă item_type='COINS')
    inventory_item_id BIGINT UNIQUE, -- Itemul din inventar (dacă item_type='INVENTORY_ITEM')

    CONSTRAINT fk_offer_trade FOREIGN KEY (trade_id) REFERENCES trades(id),
    CONSTRAINT fk_offer_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_offer_inventory_item FOREIGN KEY (inventory_item_id) REFERENCES user_inventory_items(id)
);