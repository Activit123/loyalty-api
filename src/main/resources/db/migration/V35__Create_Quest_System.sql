    -- V36__Create_Quest_System.sql
-- Creează structura de tabele pentru sistemul de Quest-uri Dinamice.

-- Tabela 1: Definiția Quest-urilor (de către Admin)
CREATE TABLE quests (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_days INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,

    -- Câmpuri Recompensă
    reward_coins INTEGER,
    reward_xp DOUBLE PRECISION,
    reward_product_id BIGINT,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_quest_reward_product FOREIGN KEY (reward_product_id) REFERENCES products(id)
);

-- Tabela 2: Definiția Criteriilor Multiple (Necesare pentru Quest-uri)
CREATE TABLE quest_criteria (
    id BIGSERIAL PRIMARY KEY,
    quest_id BIGINT NOT NULL,
    criterion_type VARCHAR(50) NOT NULL,

    -- Câmpuri Criteriu
    target_category VARCHAR(50),
    target_product_id BIGINT,
    required_amount DOUBLE PRECISION NOT NULL,

    CONSTRAINT fk_criterion_quest FOREIGN KEY (quest_id) REFERENCES quests(id),
    CONSTRAINT fk_criterion_target_product FOREIGN KEY (target_product_id) REFERENCES products(id)
);


-- Tabela 3: Starea Jucătorului (Quest Log)
CREATE TABLE user_quest_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quest_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL, -- ACTIVE, COMPLETED, REWARDED
    start_date DATE NOT NULL,
    completion_date TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_log_quest FOREIGN KEY (quest_id) REFERENCES quests(id),

    -- Un utilizator poate avea doar un singur Quest ACTIVE/COMPLETED/REWARDED pentru un QuestID dat
    CONSTRAINT uc_unique_quest_per_user UNIQUE (user_id, quest_id)
);


-- Tabela 4: Progresul Jucătorului per Criteriu (Progresul Curent)
CREATE TABLE user_criterion_progress (
    id BIGSERIAL PRIMARY KEY,
    log_id BIGINT NOT NULL,
    criterion_id BIGINT NOT NULL,
    current_progress DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,

    -- Cheia unică pentru a asigura că nu avem duplicate la nivel de log + criteriu
    unique_key VARCHAR(255) UNIQUE NOT NULL,

    CONSTRAINT fk_progress_log FOREIGN KEY (log_id) REFERENCES user_quest_log(id),
    CONSTRAINT fk_progress_criterion FOREIGN KEY (criterion_id) REFERENCES quest_criteria(id)
);