-- Users table
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

-- Persons table
CREATE TABLE IF NOT EXISTS persons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_person_name ON persons(name);
CREATE INDEX IF NOT EXISTS idx_person_role ON persons(role);

-- Genres table
CREATE TABLE IF NOT EXISTS genres (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_genre_name ON genres(name);

-- Movies table
CREATE TABLE IF NOT EXISTS movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    trailer_url TEXT NOT NULL,
    description TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes >= 1),
    release_date DATE NOT NULL,
    end_showing_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    poster_file_name VARCHAR(255) NOT NULL,
    age_rating VARCHAR(10) NOT NULL,
    CONSTRAINT check_dates CHECK (end_showing_date > release_date)
);

CREATE INDEX IF NOT EXISTS idx_movie_title ON movies(title);
CREATE INDEX IF NOT EXISTS idx_movie_status ON movies(status);
CREATE INDEX IF NOT EXISTS idx_movie_release_date ON movies(release_date);
CREATE INDEX IF NOT EXISTS idx_movie_slug ON movies(slug);
CREATE INDEX IF NOT EXISTS idx_movie_active_dates ON movies(release_date, end_showing_date);

-- Cinema Halls
CREATE TABLE IF NOT EXISTS cinema_halls (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(25) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_hall_name ON cinema_halls(name);

-- Seats
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

-- Ticket Types
CREATE TABLE IF NOT EXISTS ticket_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    display_name VARCHAR(50) NOT NULL,
    price_multiplier DECIMAL(3, 2) NOT NULL DEFAULT 1.00,
    min_age INTEGER CHECK (min_age >= 0 AND min_age <= 150),
    max_age INTEGER CHECK (max_age >= 0 AND max_age <= 150),
    requires_document BOOLEAN NOT NULL DEFAULT false,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_ticket_type_active ON ticket_types(active);
CREATE INDEX IF NOT EXISTS idx_ticket_type_code ON ticket_types(code);

-- Sessions
CREATE TABLE IF NOT EXISTS sessions (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL CHECK (base_price > 0),
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    hall_id BIGINT NOT NULL REFERENCES cinema_halls(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
);

CREATE INDEX IF NOT EXISTS idx_session_movie ON sessions(movie_id);
CREATE INDEX IF NOT EXISTS idx_session_hall ON sessions(hall_id);
CREATE INDEX IF NOT EXISTS idx_session_time ON sessions(start_time);
CREATE INDEX IF NOT EXISTS idx_session_hall_time ON sessions(hall_id, start_time);
CREATE INDEX IF NOT EXISTS idx_session_status ON sessions(status);

-- Movie-Genres many-to-many (створюється після movies і genres)
CREATE TABLE IF NOT EXISTS movie_genres (
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

-- Movie-Cast many-to-many
CREATE TABLE IF NOT EXISTS movie_cast (
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);

-- Movie-Directors many-to-many
CREATE TABLE IF NOT EXISTS movie_directors (
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);

-- Movie-Screenwriters many-to-many
CREATE TABLE IF NOT EXISTS movie_screenwriters (
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);

-- Bonus Cards
CREATE TABLE IF NOT EXISTS bonus_cards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    points_balance INTEGER NOT NULL DEFAULT 0,
    last_birthday_bonus_date DATE,
    welcome_bonus_received BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_bonus_card_user ON bonus_cards(user_id);

-- Discounts
CREATE TABLE IF NOT EXISTS discounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    percent DECIMAL(5, 2) NOT NULL CHECK (percent >= 0.01 AND percent <= 100.00),
    document_number VARCHAR(50),
    expiry_date TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    verified_at TIMESTAMP,
    verified_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_discount_user ON discounts(user_id);
CREATE INDEX IF NOT EXISTS idx_discount_active ON discounts(active);
CREATE INDEX IF NOT EXISTS idx_discount_expiry ON discounts(expiry_date);
CREATE INDEX IF NOT EXISTS idx_discount_type ON discounts(type);

-- Bookings
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

-- Payments (створюється перед tickets через UNIQUE booking_id)
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    bonus_points_used INTEGER DEFAULT 0,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(100) UNIQUE,
    payment_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_payment_booking ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payment_created ON payments(created_at);
CREATE INDEX IF NOT EXISTS idx_payment_transaction ON payments(transaction_id);

-- Tickets (створюється після payments)
CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    purchase_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    booked_seat_id BIGINT NOT NULL UNIQUE,
    payment_id BIGINT REFERENCES payments(id) ON DELETE SET NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    unique_code VARCHAR(20) NOT NULL UNIQUE,
    final_price DECIMAL(10, 2) NOT NULL CHECK (final_price >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ticket_payment ON tickets(payment_id);
CREATE INDEX IF NOT EXISTS idx_ticket_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_ticket_purchase_time ON tickets(purchase_time);
CREATE INDEX IF NOT EXISTS idx_ticket_booked_seat ON tickets(booked_seat_id);
CREATE INDEX IF NOT EXISTS idx_ticket_user ON tickets(user_id);

-- Booked Seats (створюється після tickets, без FOREIGN KEY на ticket_id спочатку)
CREATE TABLE IF NOT EXISTS booked_seats (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    seat_id BIGINT NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    session_id BIGINT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    ticket_type_id BIGINT NOT NULL REFERENCES ticket_types(id) ON DELETE CASCADE,
    ticket_id BIGINT UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    UNIQUE (session_id, seat_id)
);

CREATE INDEX IF NOT EXISTS idx_booked_seat_booking ON booked_seats(booking_id);
CREATE INDEX IF NOT EXISTS idx_booked_seat_seat_session ON booked_seats(seat_id, session_id);
CREATE INDEX IF NOT EXISTS idx_booked_seat_status ON booked_seats(status);
CREATE INDEX IF NOT EXISTS idx_booked_seat_booking_session ON booked_seats(booking_id, session_id);

-- Refunds
CREATE TABLE IF NOT EXISTS refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    ticket_id BIGINT NOT NULL UNIQUE REFERENCES tickets(id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_by VARCHAR(100),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refund_payment ON refunds(payment_id);
CREATE INDEX IF NOT EXISTS idx_refund_ticket ON refunds(ticket_id);
CREATE INDEX IF NOT EXISTS idx_refund_status ON refunds(status);
CREATE INDEX IF NOT EXISTS idx_refund_created ON refunds(created_at);

-- Bonus Rules
CREATE TABLE IF NOT EXISTS bonus_rules (
    id BIGSERIAL PRIMARY KEY,
    bonus_type VARCHAR(50) NOT NULL UNIQUE,
    points INTEGER CHECK (points >= 0),
    money_ratio DECIMAL(10, 4) CHECK (money_ratio >= 0),
    point_value DECIMAL(10, 2) NOT NULL DEFAULT 1.00 CHECK (point_value >= 0.01),
    min_points_per_transaction INTEGER NOT NULL DEFAULT 50 CHECK (min_points_per_transaction >= 1),
    max_points_per_transaction INTEGER NOT NULL DEFAULT 300 CHECK (max_points_per_transaction >= 1),
    active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bonus_rules_active ON bonus_rules(active);
CREATE INDEX IF NOT EXISTS idx_bonus_rules_type ON bonus_rules(bonus_type);

-- Bonus Transactions
CREATE TABLE IF NOT EXISTS bonus_transactions (
    id BIGSERIAL PRIMARY KEY,
    bonus_card_id BIGINT NOT NULL REFERENCES bonus_cards(id) ON DELETE CASCADE,
    payment_id BIGINT REFERENCES payments(id) ON DELETE SET NULL,
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
CREATE INDEX IF NOT EXISTS idx_bonus_trans_payment ON bonus_transactions(payment_id);
CREATE INDEX IF NOT EXISTS idx_bonus_trans_refund ON bonus_transactions(refund_id);

-- Promotions
CREATE TABLE IF NOT EXISTS promotions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    bonus_points INTEGER NOT NULL CHECK (bonus_points > 0),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_promotion_active ON promotions(active);
CREATE INDEX IF NOT EXISTS idx_promotion_dates ON promotions(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_promotion_created ON promotions(created_at);

-- Email Tokens
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

-- Додавання FOREIGN KEY між tickets.booked_seat_id і booked_seats.id
ALTER TABLE tickets ADD CONSTRAINT fk_tickets_booked_seat 
FOREIGN KEY (booked_seat_id) REFERENCES booked_seats(id) ON DELETE CASCADE;

-- Додавання FOREIGN KEY між booked_seats.ticket_id і tickets.id
ALTER TABLE booked_seats ADD CONSTRAINT fk_booked_seats_ticket 
FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE SET NULL;