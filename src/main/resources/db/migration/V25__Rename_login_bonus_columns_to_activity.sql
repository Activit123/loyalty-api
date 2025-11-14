-- Ștergem coloanele vechi care au fost create din greșeală
-- IF EXISTS previne o eroare dacă coloanele nu există
ALTER TABLE users
DROP COLUMN IF EXISTS consecutive_login_days,
DROP COLUMN IF EXISTS last_login_date;

-- Adăugăm coloanele noi cu numele corecte
ALTER TABLE users
ADD COLUMN consecutive_activity_days INTEGER NOT NULL DEFAULT 0,
ADD COLUMN last_activity_date DATE;