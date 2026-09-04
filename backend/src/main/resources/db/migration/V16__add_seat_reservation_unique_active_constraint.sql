CREATE UNIQUE INDEX idx_seat_reservation_unique_active
    ON seat_reservations (session_id, seat_id)
    WHERE status IN ('PENDING', 'CONFIRMED');
