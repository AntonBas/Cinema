CREATE INDEX IF NOT EXISTS idx_user_role_status ON users(user_role, verification_status, enabled);
CREATE INDEX IF NOT EXISTS idx_user_updated_at ON users(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_date_of_birth ON users(date_of_birth);
CREATE INDEX IF NOT EXISTS idx_user_email_trgm ON users USING gin (email gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_user_first_name_trgm ON users USING gin (first_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_user_last_name_trgm ON users USING gin (last_name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_person_name_trgm ON persons USING gin (name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_genre_name_trgm ON genres USING gin (name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_movie_title_trgm ON movies USING gin (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_movie_status_dates ON movies(status, release_date, end_showing_date);

CREATE INDEX IF NOT EXISTS idx_seat_hall_type ON seats(hall_id, seat_type);

CREATE INDEX IF NOT EXISTS idx_ticket_type_code_active ON ticket_types(code, active);

CREATE INDEX IF NOT EXISTS idx_session_movie_time ON sessions(movie_id, start_time);
CREATE INDEX IF NOT EXISTS idx_session_status_time ON sessions(status, start_time);

CREATE INDEX IF NOT EXISTS idx_booking_user_status ON bookings(user_id, status);
CREATE INDEX IF NOT EXISTS idx_booking_session_status ON bookings(session_id, status);
CREATE INDEX IF NOT EXISTS idx_booking_expires_status ON bookings(expires_at, status);

CREATE INDEX IF NOT EXISTS idx_seat_reservation_session_status ON seat_reservations(session_id, status, reserved_until);

CREATE INDEX IF NOT EXISTS idx_payment_status_created ON payments(status, created_at);
CREATE INDEX IF NOT EXISTS idx_payment_liqpay_order ON payments(liqpay_order_id);

CREATE INDEX IF NOT EXISTS idx_refund_user_status ON refunds(user_id, status);

CREATE INDEX IF NOT EXISTS idx_refund_item_refund ON refund_items(refund_id);
CREATE INDEX IF NOT EXISTS idx_refund_item_ticket ON refund_items(ticket_id);
CREATE INDEX IF NOT EXISTS idx_refund_item_status ON refund_items(status);

CREATE INDEX IF NOT EXISTS idx_ticket_user_status ON tickets(user_id, status);
CREATE INDEX IF NOT EXISTS idx_ticket_purchase_date ON tickets(DATE(purchase_time));

CREATE INDEX IF NOT EXISTS idx_bonus_card_points ON bonus_cards(points_balance);

CREATE INDEX IF NOT EXISTS idx_bonus_trans_card ON bonus_transactions(bonus_card_id);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_card_created ON bonus_transactions(bonus_card_id, created_at);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_created ON bonus_transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_type ON bonus_transactions(type);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_booking ON bonus_transactions(booking_id);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_refund ON bonus_transactions(refund_id);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_card_type ON bonus_transactions(bonus_card_id, type);

ALTER TABLE promotions ALTER COLUMN start_date TYPE DATE;
ALTER TABLE promotions ALTER COLUMN end_date TYPE DATE;

CREATE INDEX IF NOT EXISTS idx_promotion_active_dates ON promotions(active, start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_promotion_title_trgm ON promotions USING gin (title gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_user_promotion_user_redeemed ON user_promotions(user_id, redeemed_at);

CREATE INDEX IF NOT EXISTS idx_email_token_user_type ON email_tokens(user_id, type);
CREATE INDEX IF NOT EXISTS idx_email_token_expires_confirmed ON email_tokens(expires_at, confirmed);