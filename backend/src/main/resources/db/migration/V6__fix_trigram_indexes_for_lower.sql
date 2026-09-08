DROP INDEX IF EXISTS idx_movie_title_trgm;
CREATE INDEX IF NOT EXISTS idx_movie_title_trgm ON movies USING gin (lower(title) gin_trgm_ops);

DROP INDEX IF EXISTS idx_person_name_trgm;
CREATE INDEX IF NOT EXISTS idx_person_name_trgm ON persons USING gin (lower(name) gin_trgm_ops);

DROP INDEX IF EXISTS idx_genre_name_trgm;
CREATE INDEX IF NOT EXISTS idx_genre_name_trgm ON genres USING gin (lower(name) gin_trgm_ops);

DROP INDEX IF EXISTS idx_promotion_title_trgm;
CREATE INDEX IF NOT EXISTS idx_promotion_title_trgm ON promotions USING gin (lower(title) gin_trgm_ops);

DROP INDEX IF EXISTS idx_ticket_type_display_name_trgm;
CREATE INDEX IF NOT EXISTS idx_ticket_type_display_name_trgm ON ticket_types USING gin (lower(display_name) gin_trgm_ops);
