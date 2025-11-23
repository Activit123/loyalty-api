-- V37__Make_PurchaseId_Nullable.sql

-- Șterge constrângerea NOT NULL din coloana purchase_id pentru a permite NULL-uri
-- (pentru iteme acordate ca recompensă sau de către admin)
ALTER TABLE user_inventory_items
    ALTER COLUMN purchase_id DROP NOT NULL;