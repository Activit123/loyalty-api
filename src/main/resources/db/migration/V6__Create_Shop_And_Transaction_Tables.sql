-- V6__Create_Shop_And_Transaction_Tables.sql

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    cost_in_coins INTEGER NOT NULL, -- O singură coloană de cost
    image_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE coin_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount INTEGER NOT NULL,
    description TEXT,
    created_at TIMESTAMP,
    CONSTRAINT fk_transaction_user FOREIGN KEY(user_id) REFERENCES users(id)
);

CREATE TABLE shop_purchases (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    cost_at_purchase INTEGER NOT NULL,
    purchased_at TIMESTAMP,
    CONSTRAINT fk_purchase_user FOREIGN KEY(user_id) REFERENCES users(id),
    CONSTRAINT fk_purchase_product FOREIGN KEY(product_id) REFERENCES products(id)
);