ALTER TABLE users ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE payments ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE refunds ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE refunds ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE refunds ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE refunds ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE tickets ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE movies ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE movies ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE movies ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE movies ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE sessions ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE sessions ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE sessions ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE sessions ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE cinema_halls ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE cinema_halls ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE cinema_halls ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE cinema_halls ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE seats ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE seats ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE seats ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE seats ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE seat_reservations ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE seat_reservations ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE seat_reservations ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE seat_reservations ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE promotions ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE promotions ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE promotions ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE promotions ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE bonus_rules ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE bonus_rules ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE bonus_rules ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE bonus_rules ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE ticket_types ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE ticket_types ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE ticket_types ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE ticket_types ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE bonus_transactions ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE bonus_transactions ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE bonus_transactions ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE bonus_transactions ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE bonus_cards ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE bonus_cards ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE bonus_cards ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE bonus_cards ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE user_promotions ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE user_promotions ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE user_promotions ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE user_promotions ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE refund_items ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE refund_items ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;
ALTER TABLE refund_items ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);
ALTER TABLE refund_items ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;

ALTER TABLE users DROP COLUMN IF EXISTS created_at;
ALTER TABLE users DROP COLUMN IF EXISTS updated_at;

ALTER TABLE bookings DROP COLUMN IF EXISTS created_at;

ALTER TABLE payments DROP COLUMN IF EXISTS created_at;
ALTER TABLE payments DROP COLUMN IF EXISTS updated_at;

ALTER TABLE refunds DROP COLUMN IF EXISTS created_at;
ALTER TABLE refunds DROP COLUMN IF EXISTS updated_at;
ALTER TABLE refunds DROP COLUMN IF EXISTS processed_at;
ALTER TABLE refunds DROP COLUMN IF EXISTS processed_by;

ALTER TABLE tickets DROP COLUMN IF EXISTS created_at;

ALTER TABLE bonus_rules DROP COLUMN IF EXISTS updated_at;
ALTER TABLE bonus_rules DROP COLUMN IF EXISTS point_value;

ALTER TABLE bonus_transactions DROP COLUMN IF EXISTS created_at;

ALTER TABLE promotions DROP COLUMN IF EXISTS created_at;

ALTER TABLE seat_reservations DROP COLUMN IF EXISTS reserved_at;

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_changed_by ON audit_log(changed_by);
CREATE INDEX IF NOT EXISTS idx_audit_log_changed_at ON audit_log(changed_at);

DROP INDEX IF EXISTS idx_booking_created;
DROP INDEX IF EXISTS idx_payment_created;
DROP INDEX IF EXISTS idx_refund_created;
DROP INDEX IF EXISTS idx_bonus_trans_created;
DROP INDEX IF EXISTS idx_bonus_trans_card_created;
DROP INDEX IF EXISTS idx_promotion_created;
DROP INDEX IF EXISTS idx_seat_reservation_created;
DROP INDEX IF EXISTS idx_user_updated_at;

CREATE INDEX IF NOT EXISTS idx_booking_created_date ON bookings(created_date DESC);
CREATE INDEX IF NOT EXISTS idx_payment_created_date ON payments(created_date DESC);
CREATE INDEX IF NOT EXISTS idx_refund_created_date ON refunds(created_date DESC);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_created_date ON bonus_transactions(created_date DESC);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_card_created_date ON bonus_transactions(bonus_card_id, created_date DESC);
CREATE INDEX IF NOT EXISTS idx_promotion_created_date ON promotions(created_date DESC);
CREATE INDEX IF NOT EXISTS idx_user_last_modified_date ON users(last_modified_date DESC);