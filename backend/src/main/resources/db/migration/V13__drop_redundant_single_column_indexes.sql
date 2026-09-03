-- Reorder idx_booking_expires_status so the highly selective column (status)
-- leads, matching BookingRepository.findByStatusAndExpiresAtBefore (used by
-- BookingScheduler.processExpiredBookings).
DROP INDEX IF EXISTS idx_booking_expires_status;
CREATE INDEX IF NOT EXISTS idx_booking_status_expires ON bookings (status, expires_at);

-- Drop single-column indexes fully covered by the leftmost columns of an
-- existing composite index on the same table.
DROP INDEX IF EXISTS idx_movie_status;
DROP INDEX IF EXISTS idx_movie_release_date;
DROP INDEX IF EXISTS idx_ticket_type_category;
DROP INDEX IF EXISTS idx_session_movie;
DROP INDEX IF EXISTS idx_session_hall;
DROP INDEX IF EXISTS idx_session_status;
DROP INDEX IF EXISTS idx_booking_user;
DROP INDEX IF EXISTS idx_booking_session;
DROP INDEX IF EXISTS idx_booking_status;
DROP INDEX IF EXISTS idx_booking_expires;
DROP INDEX IF EXISTS idx_seat_reservation_session;
DROP INDEX IF EXISTS idx_seat_reservation_status;
DROP INDEX IF EXISTS idx_payment_status;
DROP INDEX IF EXISTS idx_refund_status;
DROP INDEX IF EXISTS idx_refund_user;
DROP INDEX IF EXISTS idx_ticket_status;
DROP INDEX IF EXISTS idx_ticket_user;
DROP INDEX IF EXISTS idx_bonus_trans_card;
DROP INDEX IF EXISTS idx_promotion_active;
DROP INDEX IF EXISTS idx_user_promotion_user;
DROP INDEX IF EXISTS idx_email_token_user;
DROP INDEX IF EXISTS idx_email_token_expires;

-- Missing index: admin audit log filters independently by action.
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_log (action);
