-- Tabela pentru coduri QR unice (Vouchere de iteme)
CREATE TABLE qr_codes (
    id UUID PRIMARY KEY, -- Codul efectiv va fi ID-ul (UUID)
    item_template_id BIGINT NOT NULL, -- Ce item primește
    is_used BOOLEAN DEFAULT FALSE, -- Dacă a fost deja scanat
    used_by_user_id BIGINT, -- Cine l-a scanat (pentru istoric)
    created_at TIMESTAMP DEFAULT NOW(),
    used_at TIMESTAMP,

    CONSTRAINT fk_qr_item_template FOREIGN KEY (item_template_id) REFERENCES item_templates(id),
    CONSTRAINT fk_qr_used_by FOREIGN KEY (used_by_user_id) REFERENCES users(id)
);