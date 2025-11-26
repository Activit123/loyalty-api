UPDATE users
SET recovery_key = SUBSTRING(
    MD5(RANDOM()::TEXT || email || NOW()::TEXT), 
    1, 
    8
)
WHERE recovery_key IS NULL;