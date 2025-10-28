-- Ștergem coloana 'status' din tabela 'tables'. Disponibilitatea
-- va fi acum calculată dinamic pe baza rezervărilor existente.
ALTER TABLE tables DROP COLUMN status;