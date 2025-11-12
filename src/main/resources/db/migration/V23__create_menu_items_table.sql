-- V1__create_menu_items_table.sql
-- Crearea tabelului menu_items
CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY, -- Folosește BIGSERIAL pentru un auto-increment de tip BIGINT
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL,
    volume VARCHAR(50),
    icon VARCHAR(50),
    order_in_menu INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Inserarea datelor inițiale
-- Folosim 't' pentru TRUE în PostgreSQL (deși TRUE funcționează de obicei, 't' e mai sigur)

-- Categoria: SOFT_DRINKS
INSERT INTO menu_items (name, description, price, category, volume, icon, order_in_menu, is_active) VALUES
('Coca-Cola Gust Original', NULL, 9, 'SOFT_DRINKS', '250 ml', '🥤', 10, TRUE),
('Coca-Cola Zero Zahăr', NULL, 9, 'SOFT_DRINKS', '250 ml', '🥤', 20, TRUE),
('Fanta Portocale', NULL, 9, 'SOFT_DRINKS', '250 ml', '🍊', 30, TRUE),
('Fanta Struguri', NULL, 9, 'SOFT_DRINKS', '250 ml', '🍇', 40, TRUE),
('Sprite Lămâie și Lămâie Verde', NULL, 9, 'SOFT_DRINKS', '250 ml', '🍋', 50, TRUE),
('Cappy Nectar Pere', NULL, 13, 'SOFT_DRINKS', '250 ml', '🍐', 60, TRUE),
('Cappy Nectar Piersică', NULL, 13, 'SOFT_DRINKS', '250 ml', '🍑', 70, TRUE),
('Cappy Nectar Portocale', NULL, 13, 'SOFT_DRINKS', '250 ml', '🟠', 80, TRUE),
('Cappy Nectar Portocale Roșii', NULL, 13, 'SOFT_DRINKS', '250 ml', '🍅', 90, TRUE),
('Cappy Nectar Vișine', NULL, 13, 'SOFT_DRINKS', '250 ml', '🍒', 100, TRUE),
('Schweppes Bitter Lemon', NULL, 11, 'SOFT_DRINKS', '250 ml', '🍋', 110, TRUE),
('Schweppes Mandarin', NULL, 11, 'SOFT_DRINKS', '250 ml', '🍊', 120, TRUE),
('Schweppes Pink Tonic', NULL, 11, 'SOFT_DRINKS', '250 ml', '🌸', 130, TRUE),
('Schweppes Tonic Water', NULL, 11, 'SOFT_DRINKS', '250 ml', '💧', 140, TRUE),
('Fuzetea Lămâie și Citronela', NULL, 13, 'SOFT_DRINKS', '250 ml', '🌿', 150, TRUE),
('Fuzetea Piersică și Hibiscus', NULL, 13, 'SOFT_DRINKS', '250 ml', '🌺', 160, TRUE),
('Apă minerală carbogazoasă Dorna', NULL, 7, 'SOFT_DRINKS', '330 ml', '💧', 170, TRUE),
('Apă plată Izvorul Alb', NULL, 7, 'SOFT_DRINKS', '330 ml', '💦', 180, TRUE);

-- Categoria: COFFEE_TEA
INSERT INTO menu_items (name, description, price, category, volume, icon, order_in_menu, is_active) VALUES
('Cafea scurtă (Brewzeus)', NULL, 10, 'COFFEE_TEA', '30 ml', '☕', 10, TRUE),
('Cafea lungă (Brewzeus)', NULL, 12, 'COFFEE_TEA', '120 ml', '☕', 20, TRUE),
('Cappuccino (Brewzeus)', NULL, 13, 'COFFEE_TEA', '120 ml', '☕', 30, TRUE),
('Ceai', 'Arome: mentă, verde cu lămâie, fructe de pădure, portocale, zmeură și căpșuni, ghimbir cu cătină, mușețel cu miere.', 8, 'COFFEE_TEA', '250 ml', '🍵', 40, TRUE);

-- Categoria: ENERGY_DRINKS
INSERT INTO menu_items (name, description, price, category, volume, icon, order_in_menu, is_active) VALUES
('Burn Original (Doză)', NULL, 11, 'ENERGY_DRINKS', '250 ml', '⚡', 10, TRUE),
('Monster Pipeline Punch (Doză)', NULL, 14, 'ENERGY_DRINKS', '500 ml', '🍹', 20, TRUE);

-- Categoria: SNACKS
INSERT INTO menu_items (name, description, price, category, volume, icon, order_in_menu, is_active) VALUES
('Alune Best (un bol)', NULL, 15, 'SNACKS', '300 g', '🥜', 10, TRUE),
('Porție Popcorn (un bol)', NULL, 15, 'SNACKS', '120 g', '🍿', 20, TRUE),
('Chipsuri Lotto Paprika', NULL, 7, 'SNACKS', '60 g', '🥔', 30, TRUE),
('Chipsuri Lotto Sare', NULL, 7, 'SNACKS', '60 g', '🥔', 40, TRUE),
('Chipsuri Lotto Smântână', NULL, 7, 'SNACKS', '60 g', '🥛', 50, TRUE),
('Pufuleți Lotto naturali', NULL, 3, 'SNACKS', '45 g', '🌽', 60, TRUE);

-- Categoria: CRAFT_BEER
INSERT INTO menu_items (name, description, price, category, volume, icon, order_in_menu, is_active) VALUES
('Weissbier (bere blondă)', '12.9 P | 8.4 EBC | 5.2%', 13, 'CRAFT_BEER', '330 ml', '🍺', 10, TRUE),
('Pale Ale (bere blondă)', '12.9 P | 7 EBC | 5.5%', 13, 'CRAFT_BEER', '330 ml', '🍺', 20, TRUE),
('Pilsner (bere blondă)', '12.4 P | 7.2 EBC | 5.1%', 13, 'CRAFT_BEER', '330 ml', '🍺', 30, TRUE),
('IPA (bere blondă)', '14.3 P | 19 EBC | 6%', 14, 'CRAFT_BEER', '330 ml', '🍺', 40, TRUE),
('Imperial Stout (bere neagră)', '20.5 P | 80.9 EBC | 9%', 14, 'CRAFT_BEER', '330 ml', '🍺', 50, TRUE),
('Cola de la munte', 'Bere fără alcool Cearfisa', 11, 'CRAFT_BEER', '330 ml', '🥤', 60, TRUE);