-- V42__Add_Recovery_Key_To_Users.sql
-- Adaugă câmpurile necesare pentru funcționalitatea de resetare a parolei fără email (Recovery Key).

-- 1. Adaugă coloana pentru cheia de recuperare (String de 8 caractere, poate fi NULL inițial)
ALTER TABLE users
    ADD COLUMN recovery_key VARCHAR(255);

-- 2. Adaugă coloana pentru a marca când a fost folosită ultima dată (pentru a preveni refolosirea)
ALTER TABLE users
    ADD COLUMN recovery_key_last_used TIMESTAMP WITHOUT TIME ZONE;

-- 3. (Opțional dar Recomandat) Populează câmpul 'recovery_key' cu o valoare unică/aleatoare
--    pentru toți utilizatorii existenți care nu au fost creați cu această logică.
--    Notă: Această operațiune poate fi lentă pe tabele mari și nu este strict necesară
--    dacă forțezi generarea cheii la prima utilizare.
--    Lăsăm doar crearea coloanei pentru a ne baza pe logica din 'registerUser'
--    pentru noii utilizatori.