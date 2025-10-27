-- Pasul 1: Adaugă coloana 'category' la 'products'
ALTER TABLE products
    ADD COLUMN category VARCHAR(50);

-- Pasul 2: Creează tabela 'races' cu noua coloană
CREATE TABLE races (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    primary_attribute VARCHAR(50),
    racial_benefit TEXT, -- Am adăugat această coloană
    loyalty_bonus_category VARCHAR(50),
    loyalty_bonus_xp_multiplier DOUBLE PRECISION
);

-- Pasul 3: Creează tabela 'class_types' (rămâne la fel)
CREATE TABLE class_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    required_attribute VARCHAR(50),
    game_type_bonus_category VARCHAR(50),
    game_type_xp_multiplier DOUBLE PRECISION,
    discount_category VARCHAR(50),
    discount_percentage DOUBLE PRECISION
);

-- Pasul 4: Adaugă noile coloane la 'users' (rămâne la fel)
ALTER TABLE users
    ADD COLUMN race_id BIGINT,
    ADD COLUMN class_type_id BIGINT,
    ADD CONSTRAINT fk_user_race FOREIGN KEY (race_id) REFERENCES races(id),
    ADD CONSTRAINT fk_user_class_type FOREIGN KEY (class_type_id) REFERENCES class_types(id);

