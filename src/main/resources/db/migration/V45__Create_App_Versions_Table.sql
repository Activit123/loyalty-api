-- resources/db/migration/V45__Create_App_Versions_Table.sql
CREATE TABLE app_versions (
    id BIGSERIAL PRIMARY KEY,
    version_code INTEGER NOT NULL, -- Ex: 10 (pentru comparare ușoară > <)
    version_name VARCHAR(50) NOT NULL, -- Ex: "1.0.0" (pentru afișare)
    is_critical BOOLEAN NOT NULL DEFAULT FALSE, -- "Urgent"
    download_url VARCHAR(255) NOT NULL,
    platform VARCHAR(20) NOT NULL DEFAULT 'ANDROID', -- Pregătit și pentru iOS
    created_at TIMESTAMP DEFAULT NOW(),
    created_by_user_id BIGINT,

    CONSTRAINT fk_version_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);