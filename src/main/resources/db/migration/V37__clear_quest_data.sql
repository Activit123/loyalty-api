-- V38__Clear_All_Quest_Data.sql
-- Șterge toate datele din tabelele Quest-urilor pentru a permite o repornire curată.

-- 1. Șterge progresul individual (UserCriterionProgress)
DELETE FROM user_criterion_progress;

-- 2. Șterge log-urile Quest-urilor utilizatorilor (UserQuestLog)
--    Acest lucru va avea succes acum că UserCriterionProgress a fost șters
DELETE FROM user_quest_log;

-- 3. Șterge definițiile criteriilor (QuestCriterion)
DELETE FROM quest_criteria;

-- 4. Șterge definițiile Quest-urilor (Quest)
DELETE FROM quests;

-- 5. (Opțional, dar recomandat) Resetează secvențele de ID-uri pentru a începe de la 1
-- AVERTISMENT: Acest lucru poate varia în funcție de DB-ul tău (PostgreSQL)
-- SELECT setval('user_criterion_progress_id_seq', 1, false);
-- SELECT setval('user_quest_log_id_seq', 1, false);
-- SELECT setval('quest_criteria_id_seq', 1, false);
-- SELECT setval('quests_id_seq', 1, false);