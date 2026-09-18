ALTER TABLE users
    ADD COLUMN IF NOT EXISTS last_verification_email_sent_at TIMESTAMP;
