ALTER TABLE sessions
    ADD COLUMN public_id UUID NOT NULL DEFAULT gen_random_uuid();

ALTER TABLE sessions
    ADD CONSTRAINT uk_sessions_public_id UNIQUE (public_id);

CREATE INDEX IF NOT EXISTS idx_session_public_id ON sessions (public_id);

ALTER TABLE bookings
    ADD COLUMN public_id UUID NOT NULL DEFAULT gen_random_uuid();

ALTER TABLE bookings
    ADD CONSTRAINT uk_bookings_public_id UNIQUE (public_id);

CREATE INDEX IF NOT EXISTS idx_booking_public_id ON bookings (public_id);
