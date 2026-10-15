ALTER TABLE bookings ADD COLUMN tickets_email_sent_at TIMESTAMPTZ NULL;

UPDATE bookings SET tickets_email_sent_at = now() WHERE status = 'CONFIRMED';

CREATE INDEX idx_booking_tickets_email_pending ON bookings (last_modified_date)
    WHERE status = 'CONFIRMED' AND tickets_email_sent_at IS NULL;
