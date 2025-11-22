-- Script de migrare pentru actualizarea prețurilor (PATCH MENU V2.0)

-- 1. Actualizare Monster Pipeline Punch la 10 RON
UPDATE menu_items
SET price = 10
WHERE name = 'Monster Pipeline Punch (Doză)';

-- 2. Actualizare tuturor sucurilor la 9 RON
-- Aceasta vizează majoritatea articolelor din SOFT_DRINKS (cu excepția apei)
UPDATE menu_items
SET price = 9
WHERE category = 'SOFT_DRINKS'
  AND name NOT LIKE '%Apă%'; -- Exclude articolele care conțin "Apă"

-- 4. Setarea explicită a apei la 7 RON (deși inițial era corect, asigurăm consistența)
UPDATE menu_items
SET price = 7
WHERE name LIKE 'Apă%';

-- 5. Lăsăm celelalte categorii (COFFEE_TEA, SNACKS, CRAFT_BEER - bere cu alcool) neschimbate.