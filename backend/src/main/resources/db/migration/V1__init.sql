CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS "user" (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'NOT_VERIFIED',
    verified_at TIMESTAMP,
    city VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    password VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    enabled BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_user_email ON "user"(email);
CREATE INDEX IF NOT EXISTS idx_user_name ON "user"(first_name, last_name);

CREATE TABLE IF NOT EXISTS person (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS genre (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_genre_name ON genre(name);

CREATE TABLE IF NOT EXISTS movie (
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
    age_rating VARCHAR(10) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_movie_title ON movie(title);
CREATE INDEX IF NOT EXISTS idx_movie_status ON movie(status);
CREATE INDEX IF NOT EXISTS idx_movie_release_date ON movie(release_date);
CREATE INDEX IF NOT EXISTS idx_movie_slug ON movie(slug);
CREATE INDEX IF NOT EXISTS idx_movie_active_dates ON movie(release_date, end_showing_date);

CREATE TABLE IF NOT EXISTS movie_genres (
    movie_id BIGINT NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genre(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

CREATE TABLE IF NOT EXISTS movie_cast (
    movie_id BIGINT NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES person(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);

CREATE TABLE IF NOT EXISTS movie_directors (
    movie_id BIGINT NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES person(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);

CREATE TABLE IF NOT EXISTS movie_screenwriters (
    movie_id BIGINT NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    person_id BIGINT NOT NULL REFERENCES person(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, person_id)
);

CREATE TABLE IF NOT EXISTS cinema_hall (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(25) NOT NULL
);

CREATE TABLE IF NOT EXISTS seat (
    id BIGSERIAL PRIMARY KEY,
    seat_row INTEGER NOT NULL,
    number INTEGER NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    hall_id BIGINT NOT NULL REFERENCES cinema_hall(id) ON DELETE CASCADE,
    UNIQUE (hall_id, seat_row, number)
);

CREATE TABLE IF NOT EXISTS session (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    base_price DECIMAL(10,2) NOT NULL CHECK (base_price > 0),
    movie_id BIGINT NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    hall_id BIGINT NOT NULL REFERENCES cinema_hall(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS promotion (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    promo_code VARCHAR(50) UNIQUE,
    discount_percentage INTEGER,
    discount_amount DECIMAL(10,2),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ticket_type (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    display_name VARCHAR(50) NOT NULL,
    price_multiplier DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    min_age INTEGER,
    max_age INTEGER,
    requires_document BOOLEAN NOT NULL DEFAULT false,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS bonus_card (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    points_balance INTEGER NOT NULL DEFAULT 0,
    total_spent DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    last_birthday_bonus_date DATE,
    welcome_bonus_received BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS discount (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    percent DECIMAL(5,2) NOT NULL,
    document_number VARCHAR(50),
    expiry_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    verified_at DATE,
    verified_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    original_amount DECIMAL(10,2),
    discount_amount DECIMAL(10,2),
    total_refunded_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(100) UNIQUE,
    payment_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    discount_id BIGINT REFERENCES discount(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ticket (
    id BIGSERIAL PRIMARY KEY,
    purchase_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    session_id BIGINT NOT NULL REFERENCES session(id) ON DELETE CASCADE,
    seat_id BIGINT NOT NULL REFERENCES seat(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    ticket_type_id BIGINT NOT NULL REFERENCES ticket_type(id) ON DELETE CASCADE,
    payment_id BIGINT REFERENCES payment(id) ON DELETE SET NULL,
    discount_id BIGINT REFERENCES discount(id) ON DELETE SET NULL,
    calculated_price DECIMAL(10,2),
    final_price DECIMAL(10,2),
    base_price_at_purchase DECIMAL(10,2),
    discount_amount DECIMAL(10,2),
    promotion_id BIGINT REFERENCES promotion(id) ON DELETE SET NULL,
    unique_code VARCHAR(20) NOT NULL UNIQUE,
    booking_type VARCHAR(10),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS refund (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payment(id) ON DELETE CASCADE,
    ticket_id BIGINT NOT NULL UNIQUE REFERENCES ticket(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_by VARCHAR(100),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bonus_transaction (
    id BIGSERIAL PRIMARY KEY,
    bonus_card_id BIGINT NOT NULL REFERENCES bonus_card(id) ON DELETE CASCADE,
    payment_id BIGINT REFERENCES payment(id) ON DELETE SET NULL,
    refund_id BIGINT REFERENCES refund(id) ON DELETE SET NULL,
    type VARCHAR(30) NOT NULL,
    points_change INTEGER NOT NULL,
    description VARCHAR(300),
    reference_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS email_token (
    token VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL,
    confirmed BOOLEAN NOT NULL DEFAULT false,
    confirmed_at TIMESTAMP,
    new_email VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_email_token_user_id ON email_token(user_id);
CREATE INDEX IF NOT EXISTS idx_email_token_expires ON email_token(expires_at);
CREATE INDEX IF NOT EXISTS idx_email_token_type ON email_token(type);

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'check_amount_positive') THEN
        ALTER TABLE payment ADD CONSTRAINT check_amount_positive CHECK (amount > 0);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'check_refund_amount_positive') THEN
        ALTER TABLE refund ADD CONSTRAINT check_refund_amount_positive CHECK (amount > 0);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'check_percent_range') THEN
        ALTER TABLE discount ADD CONSTRAINT check_percent_range CHECK (percent >= 0 AND percent <= 100);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'check_dates') THEN
        ALTER TABLE movie ADD CONSTRAINT check_dates CHECK (end_showing_date > release_date);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'check_start_time_future') THEN
        ALTER TABLE session ADD CONSTRAINT check_start_time_future CHECK (start_time >= CURRENT_TIMESTAMP);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'check_prices_positive') THEN
        ALTER TABLE ticket ADD CONSTRAINT check_prices_positive CHECK (
            (calculated_price IS NULL OR calculated_price >= 0) AND
            (final_price IS NULL OR final_price >= 0) AND
            (base_price_at_purchase IS NULL OR base_price_at_purchase >= 0) AND
            (discount_amount IS NULL OR discount_amount >= 0)
        );
    END IF;
END $$;