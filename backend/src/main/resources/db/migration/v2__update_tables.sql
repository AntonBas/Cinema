CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_id BIGINT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_booking_user ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_booking_session ON bookings(session_id);
CREATE INDEX IF NOT EXISTS idx_booking_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_booking_expires ON bookings(expires_at);

CREATE TABLE IF NOT EXISTS booked_seats (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    seat_id BIGINT NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    ticket_type_id BIGINT NOT NULL REFERENCES ticket_types(id) ON DELETE CASCADE,
    ticket_id BIGINT UNIQUE REFERENCES tickets(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_booked_seat_booking ON booked_seats(booking_id);
CREATE INDEX IF NOT EXISTS idx_booked_seat_seat_session ON booked_seats(seat_id, session_id);
CREATE INDEX IF NOT EXISTS idx_booked_seat_status ON booked_seats(status);

ALTER TABLE bonus_cards ADD COLUMN IF NOT EXISTS total_spent DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS bonus_points_used INTEGER DEFAULT 0;
ALTER TABLE payments DROP COLUMN IF EXISTS user_id;
ALTER TABLE payments DROP COLUMN IF EXISTS discount_id;
ALTER TABLE payments DROP COLUMN IF EXISTS original_amount;
ALTER TABLE payments DROP COLUMN IF EXISTS discount_amount;
ALTER TABLE refunds DROP COLUMN IF EXISTS ticket_id;
ALTER TABLE refunds ADD COLUMN IF NOT EXISTS booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE;
ALTER TABLE tickets DROP COLUMN IF EXISTS session_id;
ALTER TABLE tickets DROP COLUMN IF EXISTS seat_id;
ALTER TABLE tickets DROP COLUMN IF EXISTS user_id;
ALTER TABLE tickets DROP COLUMN IF EXISTS ticket_type_id;
ALTER TABLE tickets DROP COLUMN IF EXISTS discount_id;
ALTER TABLE tickets DROP COLUMN IF EXISTS calculated_price;
ALTER TABLE tickets DROP COLUMN IF EXISTS base_price_at_purchase;
ALTER TABLE tickets DROP COLUMN IF EXISTS discount_amount;
ALTER TABLE tickets DROP COLUMN IF EXISTS promotion_id;
ALTER TABLE tickets DROP COLUMN IF EXISTS booking_type;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS booked_seat_id BIGINT NOT NULL UNIQUE REFERENCES booked_seats(id) ON DELETE CASCADE;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS final_price DECIMAL(10, 2) NOT NULL;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE bonus_transactions DROP COLUMN IF EXISTS description;
ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_status VARCHAR(20) NOT NULL DEFAULT 'NOT_VERIFIED';
ALTER TABLE users ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS bonus_rules (
    id BIGSERIAL PRIMARY KEY,
    bonus_type VARCHAR(50) NOT NULL UNIQUE,
    points INTEGER,
    money_ratio DECIMAL(10, 4),
    point_value DECIMAL(10, 2) NOT NULL DEFAULT 1.00,
    min_points_per_transaction INTEGER NOT NULL DEFAULT 50,
    max_points_per_transaction INTEGER NOT NULL DEFAULT 300,
    is_active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bonus_rules_active ON bonus_rules(is_active);
CREATE INDEX IF NOT EXISTS idx_bonus_rules_type ON bonus_rules(bonus_type);

ALTER TABLE promotions DROP COLUMN IF EXISTS promo_code;
ALTER TABLE promotions DROP COLUMN IF EXISTS discount_percentage;
ALTER TABLE promotions DROP COLUMN IF EXISTS discount_amount;
ALTER TABLE promotions ADD COLUMN IF NOT EXISTS bonus_points INTEGER NOT NULL;

ALTER TABLE seats ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE sessions ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED';