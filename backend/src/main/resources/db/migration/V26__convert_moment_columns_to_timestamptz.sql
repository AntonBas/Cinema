DROP INDEX IF EXISTS idx_ticket_purchase_date;

ALTER TABLE users
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN verified_at TYPE timestamptz USING verified_at AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_verification_email_sent_at TYPE timestamptz USING last_verification_email_sent_at AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_password_reset_sent_at TYPE timestamptz USING last_password_reset_sent_at AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE movies
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE cinema_halls
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE seats
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE ticket_types
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE sessions
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE bookings
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN expires_at TYPE timestamptz USING expires_at AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE seat_reservations
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN reserved_until TYPE timestamptz USING reserved_until AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE payments
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN payment_time TYPE timestamptz USING payment_time AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE refunds
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE tickets
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN purchase_time TYPE timestamptz USING purchase_time AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE refund_items
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE bonus_cards
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE bonus_rules
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE bonus_transactions
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE promotions
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE user_promotions
    ALTER COLUMN created_date TYPE timestamptz USING created_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN last_modified_date TYPE timestamptz USING last_modified_date AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN redeemed_at TYPE timestamptz USING redeemed_at AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE email_tokens
    ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN expires_at TYPE timestamptz USING expires_at AT TIME ZONE 'Europe/Kyiv',
    ALTER COLUMN confirmed_at TYPE timestamptz USING confirmed_at AT TIME ZONE 'Europe/Kyiv';

ALTER TABLE audit_log
    ALTER COLUMN changed_at TYPE timestamptz USING changed_at AT TIME ZONE 'Europe/Kyiv';
