-- Vom presupune că valoarea principală era stocată în silver_coins
-- și că gold/copper nu au fost folosite încă pe scară largă.
-- Dacă trebuie să le adunăm, logica ar fi: total = copper + (silver * 10) + (gold * 100)

-- Pasul 1: Ștergem coloanele pe care le-am adăugat anterior
ALTER TABLE users DROP COLUMN copper_coins;
ALTER TABLE users DROP COLUMN gold_coins;

-- Pasul 2: Redenumim 'silver_coins' în 'coins' pentru a reflecta noul concept
ALTER TABLE users RENAME COLUMN silver_coins TO coins;