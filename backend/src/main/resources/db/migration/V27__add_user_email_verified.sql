ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT false;

UPDATE users
SET email_verified = true
WHERE enabled = true
   OR created_date IS NULL
   OR created_date < now() - INTERVAL '48 hours';

UPDATE users
SET enabled = true
WHERE email_verified = false;

ALTER TABLE users
    ALTER COLUMN enabled SET DEFAULT true;

CREATE INDEX IF NOT EXISTS idx_users_unverified_created_date
    ON users (created_date)
    WHERE email_verified = false;
