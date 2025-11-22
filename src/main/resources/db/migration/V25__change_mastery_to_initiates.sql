-- Script de migrare pentru a schimba categoria jocurilor din "Maestri" în "Inițiați"

UPDATE board_games
SET category = 'Initiati'
WHERE category = 'Maestri';