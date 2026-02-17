-- 1. Modificare QUESTS: Adăugăm coloana pentru Item Template (Recompensă)
ALTER TABLE quests
ADD COLUMN reward_item_template_id BIGINT;

ALTER TABLE quests
ADD CONSTRAINT fk_quest_reward_item_template
FOREIGN KEY (reward_item_template_id) REFERENCES item_templates(id);

-- 2. Modificare TRADE_OFFER_ITEMS: Adăugăm coloana pentru User Item (Obiectul tranzacționat)
ALTER TABLE trade_offer_items
ADD COLUMN user_item_id BIGINT;

ALTER TABLE trade_offer_items
ADD CONSTRAINT fk_trade_offer_user_item
FOREIGN KEY (user_item_id) REFERENCES user_items(id);