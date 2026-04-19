CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'NOT_VERIFIED',
    verified_at TIMESTAMP,
    city VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    password VARCHAR(72) NOT NULL,
    user_role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    enabled BOOLEAN NOT NULL DEFAULT false,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_name ON users(first_name, last_name);
CREATE INDEX IF NOT EXISTS idx_user_status ON users(verification_status);
CREATE INDEX IF NOT EXISTS idx_user_role_status ON users(user_role, verification_status, enabled);
CREATE INDEX IF NOT EXISTS idx_user_date_of_birth ON users(date_of_birth);
CREATE INDEX IF NOT EXISTS idx_user_last_modified_date ON users(last_modified_date DESC);
CREATE INDEX IF NOT EXISTS idx_user_email_trgm ON users USING gin (email gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_user_first_name_trgm ON users USING gin (first_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_user_last_name_trgm ON users USING gin (last_name gin_trgm_ops);

CREATE TABLE IF NOT EXISTS persons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_person_name ON persons(name);
CREATE INDEX IF NOT EXISTS idx_person_role ON persons(role);
CREATE INDEX IF NOT EXISTS idx_person_name_trgm ON persons USING gin (name gin_trgm_ops);

CREATE TABLE IF NOT EXISTS genres (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_genre_name ON genres(name);
CREATE INDEX IF NOT EXISTS idx_genre_name_trgm ON genres USING gin (name gin_trgm_ops);

CREATE TABLE IF NOT EXISTS movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE,
    trailer_url TEXT NOT NULL,
    description TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    release_date DATE NOT NULL,
    end_showing_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    poster_file_name VARCHAR(255) NOT NULL,
    age_rating VARCHAR(10) NOT NULL,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_movie_title ON movies(title);
CREATE INDEX IF NOT EXISTS idx_movie_status ON movies(status);
CREATE INDEX IF NOT EXISTS idx_movie_release_date ON movies(release_date);
CREATE INDEX IF NOT EXISTS idx_movie_slug ON movies(slug);
CREATE INDEX IF NOT EXISTS idx_movie_active_dates ON movies(release_date, end_showing_date);
CREATE INDEX IF NOT EXISTS idx_movie_title_trgm ON movies USING gin (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_movie_status_dates ON movies(status, release_date, end_showing_date);

CREATE TABLE IF NOT EXISTS cinema_halls (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(25) NOT NULL UNIQUE,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_hall_name ON cinema_halls(name);
CREATE INDEX IF NOT EXISTS idx_cinema_hall_name ON cinema_halls(name);

CREATE TABLE IF NOT EXISTS seats (
    id BIGSERIAL PRIMARY KEY,
    seat_row INTEGER NOT NULL,
    number INTEGER NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    hall_id BIGINT NOT NULL REFERENCES cinema_halls(id) ON DELETE CASCADE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP,
    UNIQUE (hall_id, seat_row, number)
);

CREATE INDEX IF NOT EXISTS idx_seat_hall ON seats(hall_id);
CREATE INDEX IF NOT EXISTS idx_seat_active ON seats(active);
CREATE INDEX IF NOT EXISTS idx_seat_hall_type ON seats(hall_id, seat_type);
CREATE INDEX IF NOT EXISTS idx_seat_hall_active ON seats(hall_id, active);
CREATE INDEX IF NOT EXISTS idx_seat_position ON seats(hall_id, seat_row, number);

CREATE TABLE IF NOT EXISTS ticket_types (
    id BIGSERIAL PRIMARY KEY,
    display_name VARCHAR(50) NOT NULL,
    price_multiplier DECIMAL(3, 2) NOT NULL DEFAULT 1.00,
    min_age INTEGER,
    max_age INTEGER,
    requires_document BOOLEAN NOT NULL DEFAULT false,
    document_type VARCHAR(100),
    category VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ticket_type_active ON ticket_types(active);
CREATE INDEX IF NOT EXISTS idx_ticket_type_category ON ticket_types(category);
CREATE INDEX IF NOT EXISTS idx_ticket_type_category_active ON ticket_types(category, active);
CREATE INDEX IF NOT EXISTS idx_ticket_type_display_name_trgm ON ticket_types USING gin (display_name gin_trgm_ops);

CREATE TABLE IF NOT EXISTS sessions (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    hall_id BIGINT NOT NULL REFERENCES cinema_halls(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_session_movie ON sessions(movie_id);
CREATE INDEX IF NOT EXISTS idx_session_hall ON sessions(hall_id);
CREATE INDEX IF NOT EXISTS idx_session_time ON sessions(start_time);
CREATE INDEX IF NOT EXISTS idx_session_hall_time ON sessions(hall_id, start_time);
CREATE INDEX IF NOT EXISTS idx_session_status ON sessions(status);
CREATE INDEX IF NOT EXISTS idx_session_movie_time ON sessions(movie_id, start_time);
CREATE INDEX IF NOT EXISTS idx_session_status_time ON sessions(status, start_time);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_id BIGINT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_price DECIMAL(10, 2) NOT NULL,
    bonus_points_used INTEGER DEFAULT 0,
    bonus_discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    final_price DECIMAL(10, 2) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_booking_user ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_booking_session ON bookings(session_id);
CREATE INDEX IF NOT EXISTS idx_booking_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_booking_expires ON bookings(expires_at);
CREATE INDEX IF NOT EXISTS idx_booking_final_price ON bookings(final_price);
CREATE INDEX IF NOT EXISTS idx_booking_user_status ON bookings(user_id, status);
CREATE INDEX IF NOT EXISTS idx_booking_session_status ON bookings(session_id, status);
CREATE INDEX IF NOT EXISTS idx_booking_expires_status ON bookings(expires_at, status);
CREATE INDEX IF NOT EXISTS idx_booking_created_date ON bookings(created_date DESC);
CREATE INDEX IF NOT EXISTS idx_booking_user_created ON bookings(user_id, created_date DESC);

CREATE TABLE IF NOT EXISTS seat_reservations (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id) ON DELETE CASCADE,
    seat_id BIGINT NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    session_id BIGINT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    ticket_type_id BIGINT REFERENCES ticket_types(id) ON DELETE CASCADE,
    seat_price DECIMAL(10, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reserved_until TIMESTAMP NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_seat_reservation_booking ON seat_reservations(booking_id);
CREATE INDEX IF NOT EXISTS idx_seat_reservation_session ON seat_reservations(session_id);
CREATE INDEX IF NOT EXISTS idx_seat_reservation_seat ON seat_reservations(seat_id);
CREATE INDEX IF NOT EXISTS idx_seat_reservation_status ON seat_reservations(status);
CREATE INDEX IF NOT EXISTS idx_seat_reservation_reserved_until ON seat_reservations(reserved_until);
CREATE INDEX IF NOT EXISTS idx_seat_reservation_composite ON seat_reservations(session_id, seat_id, status);
CREATE INDEX IF NOT EXISTS idx_seat_reservation_active ON seat_reservations(status, reserved_until);
CREATE INDEX IF NOT EXISTS idx_seat_reservation_user ON seat_reservations(user_id);
CREATE INDEX IF NOT EXISTS idx_seat_reservation_session_status ON seat_reservations(session_id, status, reserved_until);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    liqpay_order_id VARCHAR(50) UNIQUE,
    liqpay_payment_id VARCHAR(50),
    liqpay_transaction_id VARCHAR(100),
    liqpay_error_code VARCHAR(50),
    liqpay_error_description VARCHAR(500),
    liqpay_sender_card_mask VARCHAR(20),
    payment_time TIMESTAMP,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payment_booking ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payment_transaction ON payments(liqpay_order_id);
CREATE INDEX IF NOT EXISTS idx_payment_status_created ON payments(status, created_date);
CREATE INDEX IF NOT EXISTS idx_payment_created_date ON payments(created_date DESC);
CREATE INDEX IF NOT EXISTS idx_payment_liqpay_order ON payments(liqpay_order_id);

CREATE TABLE IF NOT EXISTS refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_amount DECIMAL(10, 2) NOT NULL,
    total_bonus_points_to_deduct INTEGER DEFAULT 0,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refund_payment ON refunds(payment_id);
CREATE INDEX IF NOT EXISTS idx_refund_status ON refunds(status);
CREATE INDEX IF NOT EXISTS idx_refund_user ON refunds(user_id);
CREATE INDEX IF NOT EXISTS idx_refund_user_status ON refunds(user_id, status);
CREATE INDEX IF NOT EXISTS idx_refund_created_date ON refunds(created_date DESC);

CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ticket_type_id BIGINT NOT NULL REFERENCES ticket_types(id) ON DELETE CASCADE,
    purchase_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_id BIGINT REFERENCES payments(id) ON DELETE SET NULL,
    seat_reservation_id BIGINT REFERENCES seat_reservations(id),
    original_price DECIMAL(10, 2) NOT NULL,
    final_price DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    unique_code VARCHAR(20) NOT NULL UNIQUE,
    bonus_points_used INTEGER DEFAULT 0,
    bonus_points_earned INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    refund_id BIGINT REFERENCES refunds(id) ON DELETE SET NULL,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ticket_booking ON tickets(booking_id);
CREATE INDEX IF NOT EXISTS idx_ticket_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_ticket_purchase_time ON tickets(purchase_time);
CREATE INDEX IF NOT EXISTS idx_ticket_ticket_type ON tickets(ticket_type_id);
CREATE INDEX IF NOT EXISTS idx_ticket_unique_code ON tickets(unique_code);
CREATE INDEX IF NOT EXISTS idx_ticket_user ON tickets(user_id);
CREATE INDEX IF NOT EXISTS idx_ticket_seat_reservation ON tickets(seat_reservation_id);
CREATE INDEX IF NOT EXISTS idx_ticket_user_status ON tickets(user_id, status);
CREATE INDEX IF NOT EXISTS idx_ticket_purchase_date ON tickets(DATE(purchase_time));

CREATE TABLE IF NOT EXISTS refund_items (
    id BIGSERIAL PRIMARY KEY,
    refund_id BIGINT NOT NULL REFERENCES refunds(id) ON DELETE CASCADE,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    ticket_price DECIMAL(10, 2) NOT NULL,
    refund_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    refund_amount DECIMAL(10, 2),
    bonus_points_to_deduct INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refund_item_refund ON refund_items(refund_id);
CREATE INDEX IF NOT EXISTS idx_refund_item_ticket ON refund_items(ticket_id);
CREATE INDEX IF NOT EXISTS idx_refund_item_status ON refund_items(status);

CREATE TABLE IF NOT EXISTS movie_genres (
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

CREATE TABLE IF NOT EXISTS movie_cast (
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);

CREATE TABLE IF NOT EXISTS movie_directors (
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);

CREATE TABLE IF NOT EXISTS movie_screenwriters (
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);

CREATE TABLE IF NOT EXISTS bonus_cards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    points_balance INTEGER NOT NULL DEFAULT 0,
    last_birthday_bonus_date DATE,
    welcome_bonus_received BOOLEAN NOT NULL DEFAULT false,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bonus_card_user ON bonus_cards(user_id);
CREATE INDEX IF NOT EXISTS idx_bonus_card_points ON bonus_cards(points_balance);

CREATE TABLE IF NOT EXISTS bonus_rules (
    id BIGSERIAL PRIMARY KEY,
    bonus_type VARCHAR(50) NOT NULL UNIQUE,
    points INTEGER,
    money_ratio DECIMAL(10, 4),
    min_points_per_transaction INTEGER,
    max_points_per_transaction INTEGER,
    active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bonus_rules_active ON bonus_rules(active);
CREATE INDEX IF NOT EXISTS idx_bonus_rules_type ON bonus_rules(bonus_type);

CREATE TABLE IF NOT EXISTS bonus_transactions (
    id BIGSERIAL PRIMARY KEY,
    bonus_card_id BIGINT NOT NULL REFERENCES bonus_cards(id) ON DELETE CASCADE,
    booking_id BIGINT REFERENCES bookings(id) ON DELETE SET NULL,
    refund_id BIGINT REFERENCES refunds(id) ON DELETE SET NULL,
    type VARCHAR(30) NOT NULL,
    points_change INTEGER NOT NULL,
    reference_id VARCHAR(50),
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bonus_trans_card ON bonus_transactions(bonus_card_id);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_type ON bonus_transactions(type);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_booking ON bonus_transactions(booking_id);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_refund ON bonus_transactions(refund_id);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_card_type ON bonus_transactions(bonus_card_id, type);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_created_date ON bonus_transactions(created_date DESC);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_card_created_date ON bonus_transactions(bonus_card_id, created_date DESC);

CREATE TABLE IF NOT EXISTS promotions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(150),
    bonus_points INTEGER NOT NULL,
    start_date DATE,
    end_date DATE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_promotion_active ON promotions(active);
CREATE INDEX IF NOT EXISTS idx_promotion_dates ON promotions(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_promotion_active_dates ON promotions(active, start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_promotion_title_trgm ON promotions USING gin (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_promotion_created_date ON promotions(created_date DESC);

CREATE TABLE IF NOT EXISTS user_promotions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    promotion_id BIGINT NOT NULL REFERENCES promotions(id) ON DELETE CASCADE,
    redeemed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    points_awarded INTEGER NOT NULL,
    created_by VARCHAR(100),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(100),
    last_modified_date TIMESTAMP,
    UNIQUE (user_id, promotion_id)
);

CREATE INDEX IF NOT EXISTS idx_user_promotion_user ON user_promotions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_promotion_promotion ON user_promotions(promotion_id);
CREATE INDEX IF NOT EXISTS idx_user_promotion_redeemed ON user_promotions(redeemed_at);
CREATE INDEX IF NOT EXISTS idx_user_promotion_user_redeemed ON user_promotions(user_id, redeemed_at);

CREATE TABLE IF NOT EXISTS email_tokens (
    token VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL,
    confirmed BOOLEAN NOT NULL DEFAULT false,
    confirmed_at TIMESTAMP,
    new_email VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_email_token_user_id ON email_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_email_token_expires ON email_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_email_token_type ON email_tokens(type);
CREATE INDEX IF NOT EXISTS idx_email_token_user ON email_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_email_token_user_type ON email_tokens(user_id, type);
CREATE INDEX IF NOT EXISTS idx_email_token_expires_confirmed ON email_tokens(expires_at, confirmed);
CREATE INDEX IF NOT EXISTS idx_email_token_token ON email_tokens(token);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    target_info VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_changed_by ON audit_log(changed_by);
CREATE INDEX IF NOT EXISTS idx_audit_log_changed_at ON audit_log(changed_at);

CREATE TABLE IF NOT EXISTS audit_log_details (
    id BIGSERIAL PRIMARY KEY,
    audit_log_id BIGINT NOT NULL REFERENCES audit_log(id) ON DELETE CASCADE,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT
);

CREATE INDEX IF NOT EXISTS idx_audit_log_details_log_id ON audit_log_details(audit_log_id);