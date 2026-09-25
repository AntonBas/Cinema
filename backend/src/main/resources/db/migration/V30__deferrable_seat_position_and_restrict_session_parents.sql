DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    SELECT con.conname INTO constraint_name
    FROM pg_constraint con
             JOIN pg_class rel ON rel.oid = con.conrelid
    WHERE rel.relname = 'seats'
      AND con.contype = 'u'
      AND con.conkey::smallint[] @> ARRAY(
            SELECT attnum FROM pg_attribute
            WHERE attrelid = rel.oid AND attname IN ('hall_id', 'seat_row', 'number'))::smallint[]
      AND array_length(con.conkey, 1) = 3;

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE seats DROP CONSTRAINT %I', constraint_name);
    END IF;
END $$;

ALTER TABLE seats
    ADD CONSTRAINT uq_seat_hall_row_number UNIQUE (hall_id, seat_row, number) DEFERRABLE INITIALLY DEFERRED;

DO $$
DECLARE
    fk RECORD;
BEGIN
    FOR fk IN
        SELECT con.conname
        FROM pg_constraint con
                 JOIN pg_class rel ON rel.oid = con.conrelid
                 JOIN pg_class ref ON ref.oid = con.confrelid
        WHERE con.contype = 'f'
          AND rel.relname = 'sessions'
          AND ref.relname IN ('cinema_halls', 'movies')
    LOOP
        EXECUTE format('ALTER TABLE sessions DROP CONSTRAINT %I', fk.conname);
    END LOOP;
END $$;

ALTER TABLE sessions
    ADD CONSTRAINT fk_sessions_hall FOREIGN KEY (hall_id) REFERENCES cinema_halls (id) ON DELETE RESTRICT;

ALTER TABLE sessions
    ADD CONSTRAINT fk_sessions_movie FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE RESTRICT;
