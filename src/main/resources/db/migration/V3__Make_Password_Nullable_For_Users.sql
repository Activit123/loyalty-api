-- Modificăm constrângerea coloanei 'password' pentru a permite valori nule.
-- Acest lucru este necesar pentru conturile create prin provideri OAuth2 (ex: Google)
-- care nu au o parolă locală.
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;