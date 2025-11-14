-- Migration to re-balance product claim values for the loyalty program.
-- The new logic sets claim_value to approximately buy_price / 5.

-- Snacks and Drinks
UPDATE products SET claim_value = 1 WHERE name = 'Alune Best (Un bol)';
UPDATE products SET claim_value = 1 WHERE name = 'Burn Original, Doza';
UPDATE products SET claim_value = 1 WHERE name = 'Cappy Nectar Pere, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Cappy Nectar Piersica, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Cappy Nectar Portocale Rosii, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Cappy Nectar Portocale, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Cappy Nectar Visine, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Coca-Cola Gust Original, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Coca-Cola Zero Zahar, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Dorna, Apa minerala naturala carbogazoasa';
UPDATE products SET claim_value = 1 WHERE name = 'Fanta Portocale, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Fanta Struguri, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Fuzetea Lamaie si Citronela, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Fuzetea Piersica si Hibiscus, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Izvorul Alb, Apa minerala naturala plata, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Loto Chips Paprika';
UPDATE products SET claim_value = 1 WHERE name = 'Loto Chips Sare';
UPDATE products SET claim_value = 1 WHERE name = 'Loto Chips Smantana';
UPDATE products SET claim_value = 1 WHERE name = 'Monster Pipeline Punch, Doza';
UPDATE products SET claim_value = 1 WHERE name = 'Portie Popcorn (Un bol)';
UPDATE products SET claim_value = 1 WHERE name = 'Pufuleti Loto Naturali (o punga)'; -- Manually set to 1 for engagement
UPDATE products SET claim_value = 1 WHERE name = 'Schweppes Bitter Lemon, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Schweppes Mandarin, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Schweppes Pink Tonic, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Schweppes Tonic Water, Sticla';
UPDATE products SET claim_value = 1 WHERE name = 'Sprite Lamaie si Lamaie Verde, Sticla';

-- Coffee and Tea
UPDATE products SET claim_value = 1 WHERE name = 'Cafea scurta Brewzeus Coffe';
UPDATE products SET claim_value = 1 WHERE name = 'Cafea lunga Brewzeus Coffe';
UPDATE products SET claim_value = 1 WHERE name = 'Cappucino Brewzeus Coffe';
UPDATE products SET claim_value = 1 WHERE name = 'Ceai';

-- Beer
UPDATE products SET claim_value = 1 WHERE name = 'Weissbier (Cearfisa)';
UPDATE products SET claim_value = 1 WHERE name = 'Pale Ale (Cearfisa)';
UPDATE products SET claim_value = 1 WHERE name = 'Pilsner (Cearfisa)';
UPDATE products SET claim_value = 1 WHERE name = 'IPA (Cearfisa)';
UPDATE products SET claim_value = 1 WHERE name = 'Imperial Stout (Cearfisa)';
UPDATE products SET claim_value = 1 WHERE name = 'Cola de munte (Bere fara alcool de la Cearfisa)';

-- Fees and Subscriptions
UPDATE products SET claim_value = 2 WHERE name = 'Taxa Intrare Standard';
UPDATE products SET claim_value = 4 WHERE name = 'Taxa Acces Jocuri Maestri (pe masa)';
UPDATE products SET claim_value = 28 WHERE name = 'Abonament Lunar Legende';

-- End of migration script.