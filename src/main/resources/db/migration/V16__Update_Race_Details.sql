-- Actualizăm descrierile și adăugăm detalii despre bonusul de loialitate
-- pentru fiecare rasă în parte.

UPDATE races
SET
    description = 'Maeștri ai diplomației și comerțului, Oamenii sunt cunoscuți pentru spiritul lor de echipă și capacitatea de a prospera în comunități. Ei prețuiesc angajamentul pe termen lung.',
    racial_benefit = 'Primesc +100% XP Bonus la achiziționarea unui Abonament Lunar, recompensând loialitatea maximă.'
WHERE name = 'OAMENI';

UPDATE races
SET
    description = 'Agili și cu simțuri ascuțite, Elfii sunt strategi rapizi care preferă finețea în locul forței brute. Se bazează pe reflexe rapide și energie constantă.',
    racial_benefit = 'Primesc +5% XP Bonus la cumpărarea de Băuturi Răcoritoare sau Energizante, menținându-le viteza de reacție.'
WHERE name = 'ELFI';

UPDATE races
SET
    description = 'Construcți solizi și rezistenți, Piticii sunt maeștri ai rezistenței și se bucură de plăcerile simple și consistente, precum o halbă de bere și o gustare bună.',
    racial_benefit = 'Primesc +5% XP Bonus la achiziționarea de Bere sau Gustări Sărate (Snacks, Chipsuri, Alune, Popcorn).'
WHERE name = 'PITICI';

UPDATE races
SET
    description = 'Impulsivi și de o vitalitate de neoprit, Orcii găsesc valoare în prezența fizică și în a fi mereu în centrul acțiunii. Pentru ei, a fi prezent este cel mai important.',
    racial_benefit = 'Primesc +15% XP Bonus pentru fiecare Taxă de Intrare achitată, încurajând prezența constantă la Bârlog.'
WHERE name = 'ORCI';