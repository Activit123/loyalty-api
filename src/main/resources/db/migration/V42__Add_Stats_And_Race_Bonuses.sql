-- 1. Modificare tabel USERS (Atributele Jucătorului)
ALTER TABLE users
ADD COLUMN strength INTEGER DEFAULT 1,
ADD COLUMN dexterity INTEGER DEFAULT 1,
ADD COLUMN intelligence INTEGER DEFAULT 1,
ADD COLUMN charisma INTEGER DEFAULT 1,
ADD COLUMN unallocated_points INTEGER DEFAULT 0;

-- 2. Modificare tabel RACES (Atributele de Start ale Rasei)
ALTER TABLE races
ADD COLUMN base_str INTEGER DEFAULT 1,
ADD COLUMN base_dex INTEGER DEFAULT 1,
ADD COLUMN base_int INTEGER DEFAULT 1,
ADD COLUMN base_cha INTEGER DEFAULT 1;

-- 3. Modificare tabel ITEM_TEMPLATES (Cerințe pentru echipare)
ALTER TABLE item_templates
ADD COLUMN req_str INTEGER DEFAULT 0,
ADD COLUMN req_dex INTEGER DEFAULT 0,
ADD COLUMN req_int INTEGER DEFAULT 0,
ADD COLUMN req_cha INTEGER DEFAULT 0;

-- 4. ACTUALIZARE RASE (Lore-ul aplicației - Valori de start)
-- OAMENI (Echilibrați)
UPDATE races SET base_str=2, base_dex=2, base_int=2, base_cha=2 WHERE name='OAMENI';

-- ELFI (Agili și Deștepți)
UPDATE races SET base_str=1, base_dex=4, base_int=3, base_cha=2 WHERE name='ELFI';

-- PITICI (Puternici și Carismatici în felul lor)
UPDATE races SET base_str=4, base_dex=1, base_int=1, base_cha=3 WHERE name='PITICI';

-- ORCI (Forță brută)
UPDATE races SET base_str=5, base_dex=2, base_int=1, base_cha=1 WHERE name='ORCI';