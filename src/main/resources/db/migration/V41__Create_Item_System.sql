-- 1. Tabela pentru Definiția Itemelor (Adminul le creează aici)
CREATE TABLE item_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(255),

    -- Configurare RPG
    slot VARCHAR(50) NOT NULL,      -- HEAD, BODY, MAIN_HAND, OFF_HAND, ACCESSORY
    rarity VARCHAR(50) NOT NULL,    -- COMMON, RARE, EPIC, LEGENDARY
    min_level INTEGER DEFAULT 1,    -- Restricție de nivel

    -- Economie
    buy_price INTEGER NOT NULL,     -- Cât costă în monede
    sell_price INTEGER,             -- Cât primești dacă îl vinzi înapoi (opțional)

    is_active BOOLEAN DEFAULT TRUE, -- Adminul poate ascunde iteme
    created_at TIMESTAMP DEFAULT NOW()
);

-- 2. Tabela pentru Efecte (Sistemul LEGO)
-- Un item poate avea oricâte efecte
CREATE TABLE item_effects (
    id BIGSERIAL PRIMARY KEY,
    item_template_id BIGINT NOT NULL,

    effect_type VARCHAR(50) NOT NULL, -- XP_BOOST, DISCOUNT, etc.
    effect_value DOUBLE PRECISION NOT NULL, -- ex: 10.0 (pentru 10%)

    -- Opțional: Dacă efectul se aplică doar la ceva specific (ex: reducere doar la BERE)
    target_category VARCHAR(50),

    CONSTRAINT fk_effect_item_template FOREIGN KEY (item_template_id) REFERENCES item_templates(id) ON DELETE CASCADE
);

-- 3. Tabela pentru Inventarul Utilizatorilor
-- Leagă un User de un ItemTemplate
CREATE TABLE user_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_template_id BIGINT NOT NULL,

    is_equipped BOOLEAN DEFAULT FALSE, -- Dacă userul poartă itemul
    acquired_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_user_items_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_items_template FOREIGN KEY (item_template_id) REFERENCES item_templates(id)
);

-- Index pentru performanță (căutăm des itemele echipate ale unui user)
CREATE INDEX idx_user_items_equipped ON user_items(user_id, is_equipped);