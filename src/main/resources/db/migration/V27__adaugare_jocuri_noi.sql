-- Script de migrare pentru adăugarea jocurilor noi (PATCH JOCURI V1.3)
-- Notă: Categoriile sunt 'Legende' (pentru jocuri complexe) și 'Initiati' (pentru restul).

INSERT INTO board_games (name, description, players, play_time, age_limit, complexity_rating, category) VALUES
-- ===================================
-- JOCURI NOI: CATEGORIA LEGENDS (Complexitate 3.5+)
-- ===================================

('Gloomhaven', 'Un joc de campanie, cooperativ, bazat pe scenarii, în care jucătorii își asumă rolul unor mercenari rătăcitori într-un tărâm fantastic. Include luptă tactică pe hărți modulare.', '1-4 jucători', '60-120 min', '14+', 3.9, 'Legende'),
('Arcs: Conflict and Collapse in the Reach', 'Un joc strategic de război spațial, influență și dezvoltare, cu elemente de "bag-building" (construcție de sac) și campanie evolutivă.', '2-4 jucători', '60-90 min', '14+', 3.6, 'Legende'),
('Arcs: The Blighted Reach (Extensie)', 'Extensie majoră pentru Arcs, adaugă noi facțiuni, cărți și reguli pentru a extinde conflictul spațial.', '2-4 jucători', '60-90 min', '14+', 3.8, 'Legende'),

-- ===================================
-- JOCURI NOI: CATEGORIA INITIATI (Complexitate Medie-Mică)
-- ===================================

('Wingspan', 'Un joc de colecție de seturi și motor de resurse, în care jucătorii atrag cele mai bune păsări în rezervațiile lor.', '1-5 jucători', '40-70 min', '10+', 2.4, 'Initiati'),
('Earth', 'Construiește ecosisteme bazate pe cărți, generează venituri de resurse și acumulează puncte de victorie. Motor de construcție rapid.', '1-5 jucători', '45-90 min', '13+', 2.5, 'Initiati'),
('Here to Slay', 'Un joc de cărți de strategie cu fantezie, în care aduni o echipă de eroi pentru a ucide monștri periculoși.', '2-6 jucători', '30-60 min', '10+', 1.8, 'Initiati'),
('Munchkin', 'Un joc de cărți satiric de tip "dungeon crawl", despre uciderea de monștri și trădarea prietenilor. Regulile sunt făcute să fie "încălcate".', '3-6 jucători', '60-120 min', '10+', 1.7, 'Initiati'),
('The Binding of Isaac: Four Souls', 'Un joc de cărți bazat pe popularul joc video, care implică luptă, acumulare de comori și obținerea a 4 suflete (Souls).', '1-4 jucători', '45-60 min', '14+', 2.0, 'Initiati'),
('Mindbug', 'Un duel de cărți rapid în care ambii jucători își pot fura reciproc creaturile. Necesită gândire tactică rapidă.', '2 jucători', '15-25 min', '8+', 1.5, 'Initiati'),
('The Game', 'Un joc cooperativ de plasare de cărți în care jucătorii trebuie să scape de toate cărțile pe 4 teancuri. Foarte solicitant mental.', '1-5 jucători', '20-30 min', '8+', 1.3, 'Initiati'),
('Crime Scene: London 1892', 'Un joc de deducție și rezolvare a cazurilor, cu indicii ascunse. Jucătorii lucrează ca detectivi pentru a rezolva o crimă.', '1-4 jucători', '45-60 min', '14+', 2.0, 'Initiati'),
('Cărți Escape: Castelul lui Dracula', 'O aventură de tip Escape Room bazată pe cărți, în care jucătorii rezolvă puzzle-uri pentru a evada din castel.', '1-6 jucători', '60 min', '12+', 2.3, 'Initiati'),
('Unlock! Evadează', 'O cutie cu 3 scenarii de Escape Room bazate pe cărți și o aplicație, care testează logica și gândirea laterală.', '1-6 jucători', '60 min', '10+', 2.5, 'Initiati'),
('Octus Regni', 'Un joc strategic de dominare teritorială și control de zonă, în care jucătorii luptă pentru supremație pe un regat modular.', '2-4 jucători', '45-60 min', '10+', 2.2, 'Initiati'),
('Octus Regni (Extensie)', 'Extensie care adaugă noi hărți și reguli, sporind varietatea și complexitatea strategică a jocului de bază.', '2-4 jucători', '45-60 min', '10+', 2.3, 'Initiati'),
('Nasty Neighbors', 'Un joc de petrecere rapid și comic, unde jucătorii încearcă să-și enerveze vecinii cu cele mai absurde acțiuni.', '3-8 jucători', '15 min', '14+', 1.0, 'Initiati'),
('Cards Against Humanity', 'Un joc de petrecere bazat pe umor negru, în care jucătorii completează propoziții "de-a dreptul oribile" cu cărți.', '4+ jucători', '30-90 min', '17+', 1.0, 'Initiati'),
('What Do You Meme?', 'Un joc de petrecere în care jucătorii asociază cărți de subtitrare cu cărți de imagine pentru a crea cel mai amuzant Meme.', '3+ jucători', '30-90 min', '17+', 1.0, 'Initiati'),
('Tarot', 'Set clasic de cărți de Tarot, perfect pentru ghicit, citit viitorul sau pur și simplu ca set de cărți de colecție.', '1+ jucători', 'Variabil', '16+', 1.0, 'Initiati'),
('Quest', 'Un joc de deducție socială și bluf, cu roluri secrete. Jucătorii încearcă să îndeplinească misiuni fără a fi sabotați de forțele răului.', '4-10 jucători', '30 min', '10+', 1.6, 'Initiati'),
('Space Station', 'Un joc de construcție de motor și gestionare a resurselor, în care jucătorii construiesc cea mai eficientă stație spațială.', '2-4 jucători', '45-60 min', '10+', 2.1, 'Initiati'),
('Escape from the Casino of Chaos', 'Joc rapid de petrecere de tip "take-that", în care jucătorii se sabotează reciproc pentru a fi primii care evadează.', '2-6 jucători', '20-30 min', '10+', 1.5, 'Initiati'),
('Escape from the Mall', 'O cursă contra cronometru în mall-ul apocaliptic. Joc de cărți rapid de supraviețuire.', '2-6 jucători', '20-30 min', '10+', 1.5, 'Initiati'),
('Escape from the Museum', 'Un joc rapid de evadare, cu rezolvare de puzzle-uri și luptă cu gardienii. Bazat pe cărți.', '2-6 jucători', '20-30 min', '10+', 1.5, 'Initiati'),
('Escape from the Movie Studio', 'O evadare tematică dintr-un studio de film. Joc de cărți rapid și plin de umor.', '2-6 jucători', '20-30 min', '10+', 1.5, 'Initiati'),
('Don''t Drop the Soap', 'Un joc de petrecere hilar, de strategie și eliminare. Evită să scapi săpunul în cele mai proaste momente.', '2-6 jucători', '15-30 min', '18+', 1.0, 'Initiati'),
('Hold Your Hat', 'Un joc de cărți de tip "push your luck" (riscă-ți norocul), în care jucătorii încearcă să colecteze seturi, dar riscă să piardă tot.', '2-4 jucători', '15 min', '8+', 1.2, 'Initiati'),
('Plums for Trash', 'Un joc simplu de licitație și colecție de seturi, cu tematică distractivă de "scame de buzunar".', '3-5 jucători', '20-30 min', '10+', 1.5, 'Initiati'),
('Too Many Poops', 'Un joc de colecție de seturi cu tematică comică. Jucătorii încearcă să-și hrănească animalele de companie ciudate.', '2-6 jucători', '20 min', '8+', 1.0, 'Initiati');