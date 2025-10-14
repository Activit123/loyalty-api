CREATE TABLE tables (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE, -- Redenumit din table_name
    capacity INT NOT NULL,
    status VARCHAR(255) NOT NULL
);

ALTER TABLE reservations
ADD COLUMN table_id BIGINT;

-- Pasul 3: Creează constrângerea de cheie externă.
ALTER TABLE reservations
ADD CONSTRAINT fk_reservations_on_table
FOREIGN KEY (table_id) REFERENCES tables(id);