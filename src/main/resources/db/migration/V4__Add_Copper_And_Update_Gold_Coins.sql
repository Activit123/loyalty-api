-- Pasul 1: Adăugăm noua coloană pentru monedele de cupru (copper)
ALTER TABLE users ADD COLUMN copper_coins INT NOT NULL DEFAULT 0;

-- Pasul 2: Modificăm coloana existentă pentru monedele de aur (gold)
-- Mai întâi adăugăm noua coloană cu un nume temporar
ALTER TABLE users ADD COLUMN gold_coins_temp INT NOT NULL DEFAULT 0;

-- Pasul 3: Migrăm datele existente. Dacă 'has_gold_subscription' era TRUE, setăm 1 monedă de aur, altfel 0.
-- Folosim o construcție CASE pentru a face conversia
UPDATE users SET gold_coins_temp = CASE WHEN has_gold_subscription = TRUE THEN 1 ELSE 0 END;

-- Pasul 4: Ștergem vechea coloană booleană
ALTER TABLE users DROP COLUMN has_gold_subscription;

-- Pasul 5: Redenumim coloana temporară la numele final
ALTER TABLE users RENAME COLUMN gold_coins_temp TO gold_coins;