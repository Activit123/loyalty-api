ALTER TABLE users
ADD COLUMN consecutive_login_days INTEGER NOT NULL DEFAULT 0,
ADD COLUMN last_login_date DATE;