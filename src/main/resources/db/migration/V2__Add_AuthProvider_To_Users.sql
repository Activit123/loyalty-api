-- Adăugăm noua coloană permițând temporar valori nule
ALTER TABLE users ADD COLUMN auth_provider VARCHAR(50);

-- Actualizăm toate rândurile existente pentru a avea o valoare implicită
-- Presupunem că toți utilizatorii existenți s-au înregistrat local
UPDATE users SET auth_provider = 'LOCAL' WHERE auth_provider IS NULL;

-- Acum, adăugăm constrângerea NOT NULL, deoarece toate rândurile au o valoare
ALTER TABLE users ALTER COLUMN auth_provider SET NOT NULL;