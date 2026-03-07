-- Actualizăm descrierea bonusului de rasă pentru Elfi ca să includă "Vin"
UPDATE races
SET racial_benefit = 'Primesc +5% XP Bonus la cumpărarea de Băuturi Răcoritoare, Energizante sau Vin, menținându-le viteza de reacție.'
WHERE name = 'ELFI';