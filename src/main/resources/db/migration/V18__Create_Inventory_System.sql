CREATE TABLE user_inventory_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    purchase_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    claim_uid UUID NOT NULL UNIQUE,
    created_at TIMESTAMP,
    claimed_at TIMESTAMP,
    CONSTRAINT fk_inventory_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_inventory_purchase FOREIGN KEY (purchase_id) REFERENCES shop_purchases(id)
);