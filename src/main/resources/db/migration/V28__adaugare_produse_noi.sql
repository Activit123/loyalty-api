-- Script de migrare pentru adăugarea produselor noi (PATCH MENIU V2.1)

-- 1. Inserare produse noi în Categoria: SNACKS
INSERT INTO menu_items (name, description, price, category, volume, icon, order_in_menu, is_active) VALUES
-- Chipsuri Leod'or
('Chipsuri Leod''or', 'Cu diverse arome (100g)', 7, 'SNACKS', '100 g', '🥔', 70, TRUE),
('Chipsuri Leod''or (Mic)', 'Cu diverse arome (60g)', 4, 'SNACKS', '60 g', '🥔', 80, TRUE),

-- Sticks Leod'or
('Sticks Leod''or', 'Cu diverse arome', 4, 'SNACKS', '70 g', '🥨', 90, TRUE),

-- YAW! Snacks
('YAW! Baguette', 'Cu diverse arome', 4, 'SNACKS', '50 g', '🥖', 100, TRUE),
('YAW! Bakefries', 'Cu diverse arome', 4, 'SNACKS', '40 g', '🍟', 110, TRUE);

-- 2. Inserare Bere Fiartă în Categoria: CRAFT_BEER
INSERT INTO menu_items (name, description, price, category, volume, icon, order_in_menu, is_active) VALUES
('Bere fiartă', 'Băutură caldă de sezon', 12, 'CRAFT_BEER', '250 ml', '🍷', 70, TRUE);

-- Notă: Numerele 'order_in_menu' au fost alese pentru a continua logic seria existentă.