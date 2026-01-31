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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_name ON users(first_name, last_name);
CREATE INDEX IF NOT EXISTS idx_user_status ON users(verification_status);

CREATE TABLE IF NOT EXISTS persons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_person_name ON persons(name);
CREATE INDEX IF NOT EXISTS idx_person_role ON persons(role);

CREATE TABLE IF NOT EXISTS genres (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_genre_name ON genres(name);

CREATE TABLE IF NOT EXISTS movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    trailer_url TEXT NOT NULL,
    description TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    release_date DATE NOT NULL,
    end_showing_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    poster_file_name VARCHAR(255) NOT NULL,
    age_rating VARCHAR(10) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_movie_title ON movies(title);
CREATE INDEX IF NOT EXISTS idx_movie_status ON movies(status);
CREATE INDEX IF NOT EXISTS idx_movie_release_date ON movies(release_date);
CREATE INDEX IF NOT EXISTS idx_movie_slug ON movies(slug);
CREATE INDEX IF NOT EXISTS idx_movie_active_dates ON movies(release_date, end_showing_date);

CREATE TABLE IF NOT EXISTS cinema_halls (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(25) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_hall_name ON cinema_halls(name);

CREATE TABLE IF NOT EXISTS seats (
    id BIGSERIAL PRIMARY KEY,
    seat_row INTEGER NOT NULL,
    number INTEGER NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    hall_id BIGINT NOT NULL REFERENCES cinema_halls(id) ON DELETE CASCADE,
    active BOOLEAN NOT NULL DEFAULT true,
    UNIQUE (hall_id, seat_row, number)
);

CREATE INDEX IF NOT EXISTS idx_seat_hall ON seats(hall_id);
CREATE INDEX IF NOT EXISTS idx_seat_active ON seats(active);

CREATE TABLE IF NOT EXISTS ticket_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    display_name VARCHAR(50) NOT NULL,
    price_multiplier DECIMAL(3, 2) NOT NULL DEFAULT 1.00,
    min_age INTEGER,
    max_age INTEGER,
    requires_document BOOLEAN NOT NULL DEFAULT false,
    document_type VARCHAR(100),
    category VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ticket_type_active ON ticket_types(active);
CREATE INDEX IF NOT EXISTS idx_ticket_type_category ON ticket_types(category);
CREATE INDEX IF NOT EXISTS idx_ticket_type_code ON ticket_types(code);

CREATE TABLE IF NOT EXISTS sessions (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    hall_id BIGINT NOT NULL REFERENCES cinema_halls(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
);

CREATE INDEX IF NOT EXISTS idx_session_movie ON sessions(movie_id);
CREATE INDEX IF NOT EXISTS idx_session_hall ON sessions(hall_id);
CREATE INDEX IF NOT EXISTS idx_session_time ON sessions(start_time);
CREATE INDEX IF NOT EXISTS idx_session_hall_time ON sessions(hall_id, start_time);
CREATE INDEX IF NOT EXISTS idx_session_status ON sessions(status);

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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_booking_user ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_booking_session ON bookings(session_id);
CREATE INDEX IF NOT EXISTS idx_booking_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_booking_expires ON bookings(expires_at);
CREATE INDEX IF NOT EXISTS idx_booking_created ON bookings(created_at);
CREATE INDEX IF NOT EXISTS idx_booking_final_price ON bookings(final_price);

CREATE TABLE IF NOT EXISTS booked_seats (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id) ON DELETE CASCADE,
    seat_id BIGINT NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    session_id BIGINT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    ticket_type_id BIGINT NOT NULL REFERENCES ticket_types(id) ON DELETE CASCADE,
    seat_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    booked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reserved_until TIMESTAMP NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_booked_seat_booking ON booked_seats(booking_id);
CREATE INDEX IF NOT EXISTS idx_booked_seat_session ON booked_seats(session_id);
CREATE INDEX IF NOT EXISTS idx_booked_seat_seat ON booked_seats(seat_id);
CREATE INDEX IF NOT EXISTS idx_booked_seat_status ON booked_seats(status);
CREATE INDEX IF NOT EXISTS idx_booked_seat_reserved_until ON booked_seats(reserved_until);
CREATE INDEX IF NOT EXISTS idx_booked_seat_composite_status ON booked_seats(session_id, seat_id, status);
CREATE INDEX IF NOT EXISTS idx_booked_seat_created ON booked_seats(booked_at);
CREATE INDEX IF NOT EXISTS idx_booked_seat_user ON booked_seats(user_id);

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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_payment_booking ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payment_created ON payments(created_at);
CREATE INDEX IF NOT EXISTS idx_payment_transaction ON payments(liqpay_order_id);

CREATE TABLE IF NOT EXISTS refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_amount DECIMAL(10, 2) NOT NULL,
    total_bonus_points_to_deduct INTEGER DEFAULT 0,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_by VARCHAR(100),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refund_payment ON refunds(payment_id);
CREATE INDEX IF NOT EXISTS idx_refund_status ON refunds(status);
CREATE INDEX IF NOT EXISTS idx_refund_created ON refunds(created_at);
CREATE INDEX IF NOT EXISTS idx_refund_user ON refunds(user_id);

CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ticket_type_id BIGINT NOT NULL REFERENCES ticket_types(id) ON DELETE CASCADE,
    purchase_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_id BIGINT REFERENCES payments(id) ON DELETE SET NULL,
    booked_seat_id BIGINT REFERENCES booked_seats(id),
    original_price DECIMAL(10, 2) NOT NULL,
    final_price DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    unique_code VARCHAR(20) NOT NULL UNIQUE,
    bonus_points_used INTEGER DEFAULT 0,
    bonus_points_earned INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    refund_id BIGINT REFERENCES refunds(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_ticket_booking ON tickets(booking_id);
CREATE INDEX IF NOT EXISTS idx_ticket_payment ON tickets(payment_id);
CREATE INDEX IF NOT EXISTS idx_ticket_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_ticket_purchase_time ON tickets(purchase_time);
CREATE INDEX IF NOT EXISTS idx_ticket_ticket_type ON tickets(ticket_type_id);
CREATE INDEX IF NOT EXISTS idx_ticket_unique_code ON tickets(unique_code);
CREATE INDEX IF NOT EXISTS idx_ticket_user ON tickets(user_id);
CREATE INDEX IF NOT EXISTS idx_ticket_booked_seat ON tickets(booked_seat_id);

CREATE TABLE IF NOT EXISTS refund_items (
    id BIGSERIAL PRIMARY KEY,
    refund_id BIGINT NOT NULL REFERENCES refunds(id) ON DELETE CASCADE,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    ticket_price DECIMAL(10, 2) NOT NULL,
    refund_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    refund_amount DECIMAL(10, 2),
    bonus_points_to_deduct INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    welcome_bonus_received BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_bonus_card_user ON bonus_cards(user_id);

CREATE TABLE IF NOT EXISTS bonus_rules (
    id BIGSERIAL PRIMARY KEY,
    bonus_type VARCHAR(50) NOT NULL UNIQUE,
    points INTEGER,
    money_ratio DECIMAL(10, 4),
    point_value DECIMAL(10, 2) NOT NULL DEFAULT 1.00,
    min_points_per_transaction INTEGER,
    max_points_per_transaction INTEGER,
    active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bonus_trans_card ON bonus_transactions(bonus_card_id);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_card_created ON bonus_transactions(bonus_card_id, created_at);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_created ON bonus_transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_type ON bonus_transactions(type);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_booking ON bonus_transactions(booking_id);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_refund ON bonus_transactions(refund_id);

CREATE TABLE IF NOT EXISTS promotions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    bonus_points INTEGER NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_promotion_active ON promotions(active);
CREATE INDEX IF NOT EXISTS idx_promotion_dates ON promotions(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_promotion_created ON promotions(created_at);

CREATE TABLE IF NOT EXISTS user_promotions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    promotion_id BIGINT NOT NULL REFERENCES promotions(id) ON DELETE CASCADE,
    redeemed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    points_awarded INTEGER NOT NULL,
    UNIQUE (user_id, promotion_id)
);

CREATE INDEX IF NOT EXISTS idx_user_promotion_user ON user_promotions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_promotion_promotion ON user_promotions(promotion_id);
CREATE INDEX IF NOT EXISTS idx_user_promotion_redeemed ON user_promotions(redeemed_at);

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