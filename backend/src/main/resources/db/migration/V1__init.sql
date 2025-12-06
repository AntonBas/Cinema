CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    city VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    password VARCHAR(60) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    user_role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    enabled BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE email_tokens (
    token VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL,
    confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    confirmed_at TIMESTAMP,
    new_email VARCHAR(100)
);

CREATE TABLE persons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE genres (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    trailer_url VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    duration_minutes INT NOT NULL,
    release_date DATE NOT NULL,
    end_showing_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    poster_file_name VARCHAR(255) NOT NULL,
    age_rating VARCHAR(20) NOT NULL
);

CREATE TABLE cinema_halls (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(25) NOT NULL
);

CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    seat_row INT NOT NULL,
    number INT NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    hall_id BIGINT NOT NULL REFERENCES cinema_halls(id),
    UNIQUE (hall_id, seat_row, number)
);

CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    base_price NUMERIC(10,2) NOT NULL,
    movie_id BIGINT NOT NULL REFERENCES movies(id),
    hall_id BIGINT NOT NULL REFERENCES cinema_halls(id)
);

CREATE TABLE ticket_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    display_name VARCHAR(50) NOT NULL,
    price_multiplier NUMERIC(3,2) NOT NULL DEFAULT 1.0,
    min_age INT,
    max_age INT,
    requires_document BOOLEAN DEFAULT FALSE,
    description VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE promotions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    promo_code VARCHAR(50) UNIQUE,
    discount_percentage INT,
    discount_amount NUMERIC(10,2),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP
);

CREATE TABLE bonus_cards (
    id BIGSERIAL PRIMARY KEY,
    card_number VARCHAR(20) NOT NULL UNIQUE,
    bonus_points INT DEFAULT 0,
    total_spent NUMERIC(10,2) DEFAULT 0,
    discount_percentage INT DEFAULT 0,
    created_at TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id)
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    purchase_time TIMESTAMP NOT NULL,
    session_id BIGINT NOT NULL REFERENCES sessions(id),
    seat_id BIGINT NOT NULL REFERENCES seats(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    ticket_type_id BIGINT NOT NULL REFERENCES ticket_types(id),
    calculated_price NUMERIC(10,2),
    base_price_at_purchase NUMERIC(10,2),
    promotion_id BIGINT REFERENCES promotions(id),
    unique_code VARCHAR(20) UNIQUE,
    booking_type VARCHAR(50),
    status VARCHAR(50) NOT NULL
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(100) UNIQUE,
    payment_time TIMESTAMP,
    created_at TIMESTAMP,
    ticket_id BIGINT NOT NULL UNIQUE REFERENCES tickets(id),
    user_id BIGINT NOT NULL REFERENCES users(id)
);

CREATE TABLE movie_cast (
    movie_id BIGINT NOT NULL REFERENCES movies(id),
    person_id BIGINT NOT NULL REFERENCES persons(id),
    PRIMARY KEY (movie_id, person_id)
);

CREATE TABLE movie_directors (
    movie_id BIGINT NOT NULL REFERENCES movies(id),
    person_id BIGINT NOT NULL REFERENCES persons(id),
    PRIMARY KEY (movie_id, person_id)
);

CREATE TABLE movie_screenwriters (
    movie_id BIGINT NOT NULL REFERENCES movies(id),
    person_id BIGINT NOT NULL REFERENCES persons(id),
    PRIMARY KEY (movie_id, person_id)
);

CREATE TABLE movie_genres (
    movie_id BIGINT NOT NULL REFERENCES movies(id),
    genre_id BIGINT NOT NULL REFERENCES genres(id),
    PRIMARY KEY (movie_id, genre_id)
);
