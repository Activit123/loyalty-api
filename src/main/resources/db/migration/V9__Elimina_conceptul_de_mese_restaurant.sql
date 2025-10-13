-- Pasul 1: Ștergem constrângerea de cheie externă din tabelul 'reservations'.
-- Acest pas este obligatoriu înainte de a putea șterge tabelul 'restaurant_tables'.
ALTER TABLE reservations
DROP CONSTRAINT fk_reservations_on_restaurant_table;

-- Pasul 2: Acum că legătura a fost ruptă, ștergem coloana 'restaurant_table_id'
-- din tabelul 'reservations' deoarece nu mai este necesară.
ALTER TABLE reservations
DROP COLUMN restaurant_table_id;

-- Pasul 3: În final, ștergem complet tabelul 'restaurant_tables'
-- deoarece nu mai este referențiat de nicio altă parte a schemei.
DROP TABLE restaurant_tables;