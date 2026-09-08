CREATE INDEX IF NOT EXISTS idx_ticket_payment ON tickets (payment_id);
CREATE INDEX IF NOT EXISTS idx_ticket_refund ON tickets (refund_id);
CREATE INDEX IF NOT EXISTS idx_seat_reservation_ticket_type ON seat_reservations (ticket_type_id);
