-- Inserare Rase
INSERT INTO races (name, description, primary_attribute, racial_benefit, loyalty_bonus_category, loyalty_bonus_xp_multiplier) VALUES
('OAMENI', 'O rasă echilibrată, cunoscută pentru capacitatea remarcabilă de a învăța și de a se adapta oricărui rol.', '+1 la orice Atribut', 'Versatilitate: Abilitatea de a se adapta oricărei situații.', 'SUBSCRIPTION', 2.0),
('ELFI', 'Grațioși și rapizi, Elfii excelează în sarcini care necesită precizie și viteză de reacție.', '+1 Agilitate', 'Grație Eterică: Mișcări rapide și o înclinație naturală spre viteză.', 'ENERGY_DRINK', 1.05),
('PITICI', 'Robuști și rezistenți, Piticii sunt maeștri ai meșteșugului și au o anduranță de neegalat.', '+1 Tărie', 'Statornicie Robustă: Rezistență fizică superioară și o conexiune cu munca grea.', 'SNACKS_SALT', 1.05),
('ORCI', 'O rasă de o forță brută și vitalitate imensă, gata să înfrunte orice provocare direct.', '+1 Constituție', 'Voință Brute: O tărie interioară care le permite să facă față oricărei provocări.', 'ENTRY_FEE', 1.15);

-- Inserare Clase
INSERT INTO class_types (name, description, required_attribute, game_type_bonus_category, game_type_xp_multiplier, discount_category, discount_percentage) VALUES
('RĂZBOINIC', 'Se bazează pe atacuri directe și dominare în lupta corp la corp.', 'Tărie', 'GAME_WARGAME', 1.15, 'GAME_WARGAME', 0.10),
('MAG', 'Utilizează vrăji puternice, bazându-se pe cunoaștere și planificare metodică.', 'Inteligență', 'GAME_STRATEGY', 1.15, 'GAME_STRATEGY', 0.10),
('HOȚ', 'Specializat în lovituri critice, evaziune și manipularea inamicilor din umbră.', 'Agilitate', 'GAME_PARTY', 1.15, 'GAME_PARTY', 0.10),
('PREOT', 'Ocupă rolul de suport, vindecând și protejând aliații în jocurile de echipă.', 'Înțelepciune', 'GAME_COOP', 1.15, 'GAME_COOP', 0.10);