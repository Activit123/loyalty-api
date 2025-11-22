-- V34__Change_Experience_To_Double.sql

-- Schimbă tipul coloanei 'experience' din BIGINT (Long) în DOUBLE PRECISION (Double/Float)
ALTER TABLE users
    ALTER COLUMN experience TYPE DOUBLE PRECISION;

-- Schimbă tipul coloanei 'amount' din 'xp_transactions'
ALTER TABLE xp_transactions
    ALTER COLUMN amount TYPE DOUBLE PRECISION;