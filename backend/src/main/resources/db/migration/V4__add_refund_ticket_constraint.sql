ALTER TABLE refunds
    ADD COLUMN ticket_id BIGINT REFERENCES tickets (id) ON DELETE SET NULL;

UPDATE refunds r
SET ticket_id = (SELECT ri.ticket_id
                  FROM refund_items ri
                  WHERE ri.refund_id = r.id
                  ORDER BY ri.id
                  LIMIT 1)
WHERE ticket_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_refund_ticket ON refunds (ticket_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_refund_ticket_processing ON refunds (ticket_id)
    WHERE status = 'PROCESSING';
