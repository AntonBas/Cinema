DO $$
DECLARE
    fk RECORD;
BEGIN
    FOR fk IN
        SELECT con.conname, rel.relname
        FROM pg_constraint con
                 JOIN pg_class rel ON rel.oid = con.conrelid
                 JOIN pg_class ref ON ref.oid = con.confrelid
        WHERE con.contype = 'f'
          AND ref.relname = 'ticket_types'
          AND rel.relname IN ('tickets', 'seat_reservations')
    LOOP
        EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I', fk.relname, fk.conname);
    END LOOP;
END $$;

ALTER TABLE tickets
    ADD CONSTRAINT fk_tickets_ticket_type FOREIGN KEY (ticket_type_id) REFERENCES ticket_types (id) ON DELETE RESTRICT;

ALTER TABLE seat_reservations
    ADD CONSTRAINT fk_seat_reservations_ticket_type FOREIGN KEY (ticket_type_id) REFERENCES ticket_types (id)
        ON DELETE RESTRICT;
