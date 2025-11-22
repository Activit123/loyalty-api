
-- Adăugăm coloanele noi cu numele corecte
ALTER TABLE users
ADD COLUMN consecutive_activity_days INTEGER NOT NULL DEFAULT 0,
ADD COLUMN last_activity_date DATE;